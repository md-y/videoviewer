#!/bin/bash
WINDOWS_PLATFORMS='"-Djavacpp.platform.windows-x86" "-Djavacpp.platform.windows-x86_64"'
LINUX_PLATFORMS='"-Djavacpp.platform.linux-x86" "-Djavacpp.platform.linux-x86_64" "-Djavacpp.platform.linux-armhf" "-Djavacpp.platform.linux-arm64"'
OSX_PLATFORMS='"-Djavacpp.platform.macosx-x86_64"'

# Windows
mvn package -f pom.xml -P windows "-Djavacpp.platform.custom" $WINDOWS_PLATFORMS

# Linux
mvn package -f pom.xml -P linux "-Djavacpp.platform.custom" $LINUX_PLATFORMS

# OSX
mvn package -f pom.xml -P osx "-Djavacpp.platform.custom" $OSX_PLATFORMS

# All Platforms
mvn package -f pom.xml -P universal "-Djavacpp.platform.custom" $WINDOWS_PLATFORMS $LINUX_PLATFORMS $OSX_PLATFORMS