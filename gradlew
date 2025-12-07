#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

APP_HOME=$(cd "`dirname "$0"`"; pwd)

exec "$APP_HOME"/gradle/wrapper/gradle-wrapper.jar "$@"
