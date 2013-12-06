#!/bin/sh

projectname="InputServiceTest"

echo "Setting up build for $projectname"
echo ""

android update project -p RemoteClient/ --subprojects --name "$projectname" --target 5
android update project -p RemoteService/ --subprojects --name "$projectname" --target 5
