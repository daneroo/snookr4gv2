# Snookr for Groovy v2

2015-11-12: Dockerizing, and upgrading to OAuth. Need to add GET/PUT to Flickr.groovy.get|post, and update inject and sign methods.


Moved from Google Code

Wrote the build with [gradle](http://www.gradle.org/documentation)

see this [section for the application plugin](http://gradle.org/docs/current/userguide/application_plugin.html).

to build locally: `gradle installApp`

to run: `gradle run`, or better `./build/install/bin/snookr4gv2`

to distribute: `gradle distZip`

as in:

    gradle installApp
    time ./build/install/bin/snookr4gv2 --fs2json /Volumes/DarwinTime/archive/media/photo/
    # or
    time ./build/install/bin/snookr4gv2 --push /Volumes/DarwinTime/archive/media/photo/

## Docker
Had to start from a gradle image: `niaquinto/gradle`, but had to downgrade to `java:6-jdk` as a base image.
This is because we need deprecated: `com.sun.image.codec.jpeg.JPEGDecodeParam`, used from `JpegMetadataReader.readMetadata`.

The new version of graddle builds a bit differntly.

    // the name is derived from the directory name, hence app, instead of snookr4gv2
    docker build  -t daneroo/gradle .
    docker run --rm -it -v `pwd`:/usr/src/app:rw --entrypoint bash daneroo/gradle
    gradle clean installDist
    ./build/install/app/bin/app --fli2db
    ./build/install/app/bin/app --push ./imageExistTest/


## Setup
On OSX, 
 
    brew install groovy gradle