#!/usr/bin/env bash

# Install OpenJDK 17
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk

# Set JAVA_HOME and PATH
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Run Gradle build
./gradlew clean build
