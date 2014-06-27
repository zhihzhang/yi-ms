#!/bin/bash

if [ $# -lt 4 ]; then 
    echo "usage: packash.sh dir_to_package app_name, app_version platform"
    echo "example: package.sh . helloworld 0.1.1 x64_ubuntu"
    exit 0;
fi
rm -rf target
mkdir target
curl -sS 'https://raw.githubusercontent.com/yubin154/cronusagent/master/agent/scripts/cronus_package/package.sh' | DIR=$1 appName=$2 version=$3 platform=$4 bash
mv $2-$3.$4.cronus* target/

