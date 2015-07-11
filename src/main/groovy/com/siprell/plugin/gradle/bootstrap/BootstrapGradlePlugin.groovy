package com.siprell.plugin.gradle.bootstrap

import org.gradle.api.file.FileTree
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException

/*
TODO
Add to README.md:

Do not use the bootstrap subdirectories because the plugin might delete them.

./gradlew bootstrapFameworkCurrentVersion
./gradlew bFCV
./gradlew bootstrapFrameworkDefaultVersion
./gradlew bFDV

buildscript {
	ext {
	    // Bootstrap Framework releases:  https://github.com/twbs/bootstrap/tags
		bootstrapFrameworkVersion = "3.3.5" 
		bootstrapFrameworkUseIndividualJs = true
		bootstrapFrameworkUseLess = true
	}
}
*/

class BootstrapGradlePlugin implements Plugin<Project> {
	final DEFAULT_VERSION = "3.3.5"
	
	void apply(Project project) {
//
//		String bootstrapFrameworkVersion = project.hasProperty("bootstrapFrameworkVersion") ? project.bootstrapFrameworkVersion : DEFAULT_VERSION
//		boolean useIndividualJs = project.hasProperty("bootstrapFrameworkUseIndividualJs") ? project.bootstrapFrameworkUseIndividualJs : false
//		boolean useLess = project.hasProperty("bootstrapFrameworkUseLess") ? project.bootstrapFrameworkUseLess : false
		String bootstrapFrameworkVersion = project.bootstrapFrameworkVersion ?: DEFAULT_VERSION
		boolean useIndividualJs = project.bootstrapFrameworkUseIndividualJs ?: false
		boolean useLess = project.bootstrapFrameworkUseLess ?: false
		FileTree bootstrapZipTree
		String filePrefix = "bootstrap-v"
		String fileSuffix = ".zip"

		project.afterEvaluate {
			project.tasks.processResources.dependsOn("createBootstrapJs", "createBootstrapMixins")
		}

		project.task("bootstrapFrameworkCurrentVersion") << {
			println "$bootstrapFrameworkVersion is the current Bootstrap Framework version."
		}

		project.task("bootstrapFrameworkDefaultVersion") << {
			println "$DEFAULT_VERSION is the default Bootstrap Framework version."
		}

		project.task("downloadBootstrapZip") {
			def tmpDir = "${project.buildDir}/tmp"
			def bootstrapZipFile = project.file("$tmpDir/${filePrefix}${bootstrapFrameworkVersion}${fileSuffix}")
			if (bootstrapZipFile.exists()) {
				bootstrapZipTree = project.zipTree(bootstrapZipFile)
			} else {
				def url = "https://github.com/twbs/bootstrap/archive/v${bootstrapFrameworkVersion}.zip"
				try {
					def file = project.file(bootstrapZipFile).newOutputStream()
					file << new URL(url).openStream()
					file.close()
					bootstrapZipTree = project.zipTree(bootstrapZipFile)
				} catch (e) {
					project.file(bootstrapZipFile).delete()
					println "Error: Could not download $url.\nYou are not connected to the Internet, or $bootstrapFrameworkVersion is an invalid version number."
					List<File> bootstrapZipFiles = []
					project.file(tmpDir).listFiles().each {
						if (it.name.startsWith("bootstrap-v")) {
							bootstrapZipFiles << it
						}
					}
					if (bootstrapZipFiles.size() > 0) {
						File bootstrapFile
						if (bootstrapZipFiles.size() == 1) {
							bootstrapFile = bootstrapZipFiles[0]
						} else {
							//bootstrapFile = bootstrapZipFiles.toSorted().last()
							bootstrapFile = bootstrapZipFiles.sort(false) { a, b ->
								def tokens = [a.name.minus(filePrefix).minus(fileSuffix), b.name.minus(filePrefix).minus(fileSuffix)]
								tokens*.tokenize('.')*.collect { it as int }.with { u, v ->
									[u, v].transpose().findResult { x, y -> x <=> y ?: null } ?: u.size() <=> v.size()
								}
							}[-1]
						}
						bootstrapZipTree = project.zipTree(bootstrapFile)
						String oldVersion = bootstrapFrameworkVersion
						bootstrapFrameworkVersion = bootstrapFile.name.minus(filePrefix).minus(fileSuffix)
						println "Using Bootstrap Framework version $bootstrapFrameworkVersion instead of $oldVersion."
					} else {
						throw new TaskExecutionException(project.tasks.downloadBootstrapZip, new Throwable("No bootstrap zip file found in $tmpDir."))
					}
				}
			}
		}

		project.task("createBootstrapJsAll", dependsOn: project.tasks.downloadBootstrapZip) {
			def path = "grails-app/assets/javascripts"
			def file = "bootstrap-all.js"
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file file
				outputs.dir path
			}
			doLast {
				def bootstrapJs = project.file("$path/$file")
				bootstrapJs.text = """//
// Do not edit this file. It will be overwritten by the bootstrap plugin.
//
//= require bootstrap/bootstrap.js
"""
			}
		}

		project.task("createBootstrapJs", dependsOn: project.tasks.createBootstrapJsAll) {
			def path = "grails-app/assets/javascripts/bootstrap"
			if (!project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/dist/js/bootstrap.js"
				if (useIndividualJs) {
					include "*/js/*.js"
				}
			}.collect()
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file files
				outputs.dir path
			}
			doLast {
				project.copy {
					from files
					into path
				}
				if (!useIndividualJs) {
					project.file(path).listFiles().each { file ->
						if (file.name != "bootstrap.js") {
							file.delete()
						}
					}
				}
			}
		}

		project.task("createBootstrapCssAll", dependsOn: project.tasks.downloadBootstrapZip) {
			def path = "grails-app/assets/stylesheets"
			def file = "bootstrap-all.css"
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file file
				outputs.dir path
			}
			doLast {
				def bootstrapCss = project.file("$path/$file")
				bootstrapCss.text = """/*
* Do not edit this file. It will be overwritten by the bootstrap plugin.
*
*= require bootstrap/css/bootstrap.css
*= require bootstrap/css/bootstrap-theme.css
*/
"""
			}
		}

		project.task("createBootstrapFonts", dependsOn: project.tasks.createBootstrapCssAll) {
			def path = "grails-app/assets/stylesheets/bootstrap/fonts"
			if (!project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/fonts/*"
			}.collect()
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file files
				outputs.dir path
			}
			doLast {
				project.copy {
					from files
					into path
				}
			}
		}

		project.task("createBootstrapCssIndividual", dependsOn: project.tasks.createBootstrapFonts) {
			def path = "grails-app/assets/stylesheets/bootstrap/css"
			if (!project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/dist/css/*.css"
				exclude "*/dist/css/*.min.css"
			}.collect()
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file files
				outputs.dir path
			}
			doLast {
				project.copy {
					from files
					into path
				}
			}
		}

		project.task("createBootstrapLessAll", dependsOn: project.tasks.createBootstrapCssIndividual) {
			def path = "grails-app/assets/stylesheets"
			def file = "bootstrap-less.less"
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file file
				outputs.dir path
			}
			doLast {
				if (useLess) {
					def bootstrapLess = project.file("$path/$file")
					bootstrapLess.text = """/*
* This file is for your less and mixin customizations.
* It was created by the bootstrap plugin.
* It will not be overwritten.
*
* You can import all less and mixin files as shown below,
* or you can import them individually.
* See https://github.com/kensiprell/grails3-bootstrap/blob/master/README.md#less
*/

@import "bootstrap/less/bootstrap.less";

/*
* Your customizations go below this section.
*/
"""
				}
			}
		}

		project.task("createBootstrapLess", dependsOn: project.tasks.createBootstrapLessAll) {
			def path = "grails-app/assets/stylesheets/bootstrap/less"
			def files = bootstrapZipTree.matching {
				include "*/less/*.less"
			}.collect()
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file files
				outputs.dir path
			}
			doLast {
				if (useLess) {
					project.copy {
						from files
						into path
					}
				} else {
					project.file(path).deleteDir()
				}
			}
		}

		project.task("createBootstrapMixins", dependsOn: project.tasks.createBootstrapLess) {
			def path = "grails-app/assets/stylesheets/bootstrap/less/mixins"
			if (useLess && !project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/less/mixins/*.less"
			}.collect()
			project.gradle.taskGraph.whenReady { graph ->
				inputs.file files
				outputs.dir path
			}
			doLast {
				if (useLess) {
					project.copy {
						from files
						into path
					}
				} else {
					project.file(path).deleteDir()
				}
			}
		}
	}
}
