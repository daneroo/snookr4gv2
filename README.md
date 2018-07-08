# Snookr for Groovy v2

2015-11-13: Figured out how to re-install JDK6 on OSX
2015-11-12: Dockerizing, and upgrading to OAuth. Need to add GET/PUT to Flickr.groovy.get|post, and update inject and sign methods.


Moved from Google Code

Wrote the build with [gradle](http://www.gradle.org/documentation)

see this [section for the application plugin](http://gradle.org/docs/current/userguide/application_plugin.html).

to build locally: `gradle installDist`, since `installApp` is deprecated

to run: `gradle run`, or better `./build/install/bin/snookr4gv2`

to distribute: `gradle distZip`

as in:

    gradle clean installDist
    time ./build/install/snookr4gv2/bin/snookr4gv2 --fs2json /Volumes/DarwinTime/archive/media/photo/
    # or
    time ./build/install/snookr4gv2/bin/snookr4gv2 --push /Volumes/DarwinTime/archive/media/photo/

## Docker
This docker image allows us to compile the project with groovy/gradle under jdk1.6

Had to start from a gradle image: `niaquinto/gradle`, but had to downgrade to `java:6-jdk` as a base image.
This is because we need deprecated: `com.sun.image.codec.jpeg.JPEGDecodeParam`, used from `JpegMetadataReader.readMetadata`.

The new version of graddle builds a bit differntly.
```
// the name is derived from the directory name, hence app, instead of snookr4gv2
docker build  -t daneroo/gradle .
docker run --rm -it -v `pwd`:/usr/src/app:rw -v /Volumes/Space/archive:/archive daneroo/gradle
gradle clean installDist
. ENV.sh 
./build/install/snookr4gv2/bin/snookr4gv2 --fli2db
./build/install/snookr4gv2/bin/snookr4gv2 --push /archive/media/photo/
./build/install/snookr4gv2/bin/snookr4gv2 --push ./imageExistTest/
```

## Setup
On OSX, 
  ** actually I don't think I need groovy... **
    brew install groovy gradle

### Install legacy JDK6 for OSX
I had also installed the current jdk8, but this is the path to jdk6

    https://support.apple.com/kb/DL1572?locale=en_US

To list available versions"

     /usr/libexec/java_home -V

To use jdk6:

    export JAVA_HOME=`/usr/libexec/java_home -v 1.6`
