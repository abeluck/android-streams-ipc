#!/bin/bash
adb uninstall com.commonsware.android.advservice.client && adb uninstall com.commonsware.android.advservice
ant -buildfile RemoteClient/build.xml clean debug && ant -buildfile RemoteService/build.xml clean debug
adb install RemoteClient/bin/InputServiceTest-debug.apk && adb install RemoteService/bin/InputServiceTest-debug.apk 
