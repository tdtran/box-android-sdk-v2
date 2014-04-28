#!/bin/bash

set -e

# This script provides some extra functionality for building the Box Android SDK
# using Gradle. It takes one optional argument - a path to a build root. A build
# root is a directory where all artifacts and intermediates will be copied to,
# making them easier to archive.
#
# Parameters:
#     $1	An optional build root path.
#
# Environment Variables:
#     DEBUG			If set, a debug build will be installed and no artifacts
#					will be uploaded.
#     GRADLE_PROP	An optional path to a gradle.properties file that will be
#					copied into the BoxAndroidLibraryV2 directory.

build_root="$1"

if [ $DEBUG ]; then
	echo "Debug build. Artifacts will not be uploaded."
fi

if [ -n "$GRADLE_PROP" ]; then
	echo "Copying gradle.properties from $GRADLE_PROP."
	cp "$GRADLE_PROP" BoxAndroidLibraryV2/gradle.properties
fi

if [ $DEBUG ]; then
	gradle assembleDebug installArchives
else
	gradle assemble uploadArchives
fi

if [ -n "$build_root" ]; then
	mkdir -p "$build_root/Artifacts" &&
		cp -r "BoxAndroidLibraryV2/build/libs/"* "$build_root/Artifacts" &&
		cp -r "BoxSDKSample/build/apk/"* "$build_root/Artifacts" &&
		cp -r "HelloWorld2/build/apk/"* "$build_root/Artifacts" &&
		cp -r "SampleOAuth/build/apk/"* "$build_root/Artifacts"
	mkdir -p "$build_root/Intermediates" &&
		cp -r "BoxAndroidLibraryV2/build/"* "$build_root/Intermediates" &&
		cp -r "BoxSDKSample/build/"* "$build_root/Intermediates" &&
		cp -r "HelloWorld2/build/"* "$build_root/Intermediates" &&
		cp -r "SampleOAuth/build/"* "$build_root/Intermediates"
fi
