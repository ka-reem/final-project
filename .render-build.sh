#!/usr/bin/env bash

# Update package manager and install OpenJDK 17
apt-get update && apt-get install -y openjdk-17-jdk

# Verify Java installation directory
ls -la /usr/lib/jvm/

# Set JAVA_HOME for this session (using alternatives to find correct path)
export JAVA_HOME=$(update-alternatives --query java | grep 'Best: ' | cut -d ' ' -f2 | sed 's/\/bin\/java//')
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java version and path
java -version
echo "JAVA_HOME=$JAVA_HOME"

# Run Gradle build with explicit Java home
./gradlew clean build -Dorg.gradle.java.home="$JAVA_HOME"