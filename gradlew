#!/usr/bin/env sh

DIRNAME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CLASSPATH="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "Missing Gradle wrapper jar: $CLASSPATH"
  exit 1
fi

if [ -n "$JAVA_HOME" ]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
else
  JAVA_EXE=java
fi

exec "$JAVA_EXE" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
