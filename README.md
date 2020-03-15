# Snookr for Groovy v2

Deprecated - can only use existing `build` directory, so I checked it in. see docker section below

- 2020-03-14 Graddle no longer works, old http: workaround (2018) no longer works
- 2018-07-10 Workaround for maven repo over http and docker
- 2015-11-13: Figured out how to re-install JDK6 on OSX
- 2015-11-12: Dockerizing, and upgrading to OAuth. Need to add GET/PUT to Flickr.groovy.get|post, and update inject and sign methods.

## Docker

This docker image allows us to compile the project with groovy/gradle under jdk1.6

Had to start from a gradle image: `niaquinto/gradle`, but had to downgrade to `java:6-jdk` as a base image.
This is because we need deprecated: `com.sun.image.codec.jpeg.JPEGDecodeParam`, used from `JpegMetadataReader.readMetadata`.

The new version of graddle builds a bit differently.

```bash
// the name is derived from the directory name, hence app, instead of snookr4gv2
docker build  -t daneroo/gradle .
docker run --rm -it -v `pwd`:/usr/src/app:rw -v /Volumes/Space/archive:/archive daneroo/gradle

# This no longer works!!!!
# gradle clean installDist

. ENV.sh
# This still works
./build/install/snookr4gv2/bin/snookr4gv2 --fli2db
./build/install/snookr4gv2/bin/snookr4gv2 --fs2db /archive/media/photo/

# Download no longer works, but lists the urls, which can be adjusted with https and fetched with curl!
./build/install/snookr4gv2/bin/snookr4gv2 --fetch data/SnookrFetchDir/

# Don't do this, no new content
./build/install/snookr4gv2/bin/snookr4gv2 --push /archive/media/photo/
# Just an old test
./build/install/snookr4gv2/bin/snookr4gv2 --push ./imageExistTest/
```

## Legacy

### Moved from Google Code

Wrote the build with [gradle](http://www.gradle.org/documentation)

see this [section for the application plugin](http://gradle.org/docs/current/userguide/application_plugin.html).

to build locally: `gradle installDist`, since `installApp` is deprecated

to run: `gradle run`, or better `./build/install/bin/snookr4gv2`

to distribute: `gradle distZip`

as in:

```bash
gradle clean installDist
time ./build/install/snookr4gv2/bin/snookr4gv2 --fs2json /Volumes/DarwinTime/archive/media/photo/
# or
time ./build/install/snookr4gv2/bin/snookr4gv2 --push /Volumes/DarwinTime/archive/media/photo/
```

### Setup

On OSX,
  **actually I don't think I need groovy...**
    brew install groovy gradle

### Install legacy JDK6 for OSX

I had also installed the current jdk8, but this is the path to jdk6

- <https://support.apple.com/kb/DL1572?locale=en_US>

To list available versions"

- `/usr/libexec/java_home -V`

To use jdk6:

- `export JAVA_HOME=$(/usr/libexec/java_home -v 1.6)`
