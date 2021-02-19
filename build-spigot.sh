#!/bin/bash
PLUGIN_LOCATION="./testserver/plugins"

mvn package -f pom.xml "-Djavacpp.platform.host"
cp ./target/*-shaded.jar $PLUGIN_LOCATION