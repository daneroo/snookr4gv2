# Snookr for Groovy v2

Moved from Google Code

Wrote the build with [gradle](http://www.gradle.org/documentation)

see this [section for the application plugin](http://gradle.org/docs/current/userguide/application_plugin.html).

to build locally: `gradle installApp`

to run: `gradle run`, or better `./build/install/bin/snookr4gv2`

to distribute: `gradle distZip`

as in:

    gradle installApp
    time ./build/install/bin/snookr4gv2 --fs2json /Volumes/DarwinTime/archive/media/photo/


## Setup
On OSX, 
 
    brew install groovy gradle