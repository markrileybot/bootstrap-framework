## Gradle plugin for integrating the Bootstrap Framework

[Bootstrap](http://getbootstrap.com) bills itself as "the most popular HTML, CSS, and JS framework for developing responsive, mobile first projects on the web."

If you have a question, suggestion, or want to report a bug, please submit an [issue](https://github.com/kensiprell/bootstrap-framework-gradle/issues). I will reply as soon as I can.

### Highlights

* The Bootstrap version can be configured in your application's ```build.gradle```, which means you do not have to install a different version of the plugin to get a different Bootstrap version, nor do you have to wait on a plugin update to use the latest Bootstrap release.

* The plugin supports the [asset-pipeline-core](https://github.com/bertramdev/asset-pipeline-core) plugin and its [less-asset-pipeline](https://github.com/bertramdev/less-asset-pipeline) module out of the box.

### Installation

Add the following lines to your application's ```build.gradle``` changing the properties as necessary. The commented-out lines are not required for the plugin to work. 

    buildscript {
        ext {
            // The property below allows you to use a Bootstrap version other than the one shipped with the plugin.
            //bootstrapFrameworkVersion = "3.3.4"
            // Use the property below to set the location where the Bootstrap files will be copied.
            //bootstrapFrameworkAssetsPath = "src/main/webapp/resources/bootstrap"
            // Uncomment to use individual JavaScript files in your manifests or asset-pipeline tags.
            //bootstrapFrameworkUseIndividualJs = true
            // Uncomment to use LESS files (requires the less-asset-pipeline plugin).
            //bootstrapFrameworkUseLess = true
        }
        repositories {
            jcenter()
        }
    }

Add the following line to the root of your application's ```build.gradle```:

    apply plugin: "bootstrap-framework-gradle"
        
### How the Plugin Works

The plugin downloads the appropriate Bootstrap zip file and copies it to your application's ```build/tmp``` directory. The plugin will extract the necessary files and copy them to an application directory using the logic below. 

* If you set the ```bootstrapFrameworkAssetsPath``` property, the plugin will use that location.

* If you use the plugin in a Grails application, the plugin will use ```grails-app/assets```.

* If you use the plugin in a non-Grails application with the asset-pipeline plugin, it will use  ```src/assets```.

* Otherwise, it will use ```src/main/webapp/resources``` as its root directory.

The Bootstrap files are copied into directory tree shown below. which is the one used by the asset-pipeline plugin. It is important that you do not put any files in the two ```bootstrap``` directories because they will be overwritten.

    |----javascripts/
    |    |    bootstrap-all.js
    |    |----bootstrap/
    |    |    |    affix.js
    |    |    |    alert.js
    |    |    |    bootstrap.js
    |    |    |    etc.
    |----stylesheets/
    |    |    bootstrap-all.css
    |    |    bootstrap-less.less
    |    |----bootstrap/
    |    |    |----css/
    |    |    |    |    bootstrap-theme.css
    |    |    |    |    bootstrap.css
    |    |    |----fonts/
    |    |    |    |    glyphicons-halflings-regular.eot
    |    |    |    |    glyphicons-halflings-regular.svg
    |    |    |    |    glyphicons-halflings-regular.ttf
    |    |    |    |    glyphicons-halflings-regular.woff
    |    |    |    |    glyphicons-halflings-regular.woff2
    |    |    |----less/
    |    |    |    |    alerts.less
    |    |    |    |    badges.less
    |    |    |    |    etc.
    |    |    |    |----mixins/
    |    |    |    |    |    alerts.less
    |    |    |    |    |    background-variant.less
    |    |    |    |    |    etc.


### Asset Pipeline Usage

The remaining sections demonstrate how to include the Bootstrap Framework in your application using the asset-pipeline-core plugin and its less-asset-pipeline module. 

### JavaScript

The instructions below assume the manifest file is in the ```grails-app/assets/javascripts``` directory.

#### Add all Bootstrap JavaScript Files

Add the line below to a manifest:

    //= require bootstrap-all
  
Or add the line below to a GSP:

    <asset:javascript src="bootstrap-all.js"/>

#### Add individual Bootstrap JavaScript files

Ensure you set the parameter below to true as described above:

    bootstrapFrameworkUseIndividualJs = true

Add a line similar to the one below to a manifest:

    //= require bootstrap/bootstrap-affix
  
Or add the line below to a GSP:

    <asset:javascript src="bootstrap/bootstrap-affix.js"/>

### Stylesheets

The instructions below assume the manifest file is in the ```grails-app/assets/stylesheets``` directory.

#### Add all Bootstrap CSS Files

Add the line below to a manifest:

    *= require bootstrap-all
  
Or add the line below to a GSP:

    <asset:stylesheet src="bootstrap-all.css"/>

#### Add individual Bootstrap CSS Files

Add the line below to a manifest:

    *= require bootstrap/css/bootstrap-theme
  
Or add the line below to a GSP:

    <asset:stylesheet src="bootstrap/css/bootstrap-theme.css"/>

### LESS

#### Add LESS Files

Add the line below to a manifest:

    *= require bootstrap-less
  
Or add the line below to a GSP:

    <asset:stylesheet src="bootstrap-less.css"/>

#### LESS Customizations

The create the file below in your application's ```grails-app/assets/stylesheets``` directory when it is first installed. Use it for customizing the framework.

    /*
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

### Glyphicons

The Glyphicons icons are available as described in the [Boostrap Components](http://getbootstrap.com/components/) section.

