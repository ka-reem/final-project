#!/usr/bin/env bash

# Update package manager and install ncurses (provides tput)
apt-get update && apt-get install -y ncurses-bin

# Set JAVA_HOME for this session (optional, if needed)
export JAVA_HOME=/opt/render/.java/openjdk-17
export PATH=$JAVA_HOME/bin:$PATH

# Run Gradle build
./gradlew clean build
