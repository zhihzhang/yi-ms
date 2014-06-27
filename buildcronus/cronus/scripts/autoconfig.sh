#!/bin/bash
# 
# Use This Script for Auto Configuration
#
# author: mixia@ebay.com or Minhan.Xia@gmail.com

abspath=$(cd ${0%/*} && echo $PWD/${0##*/})
scripts_dir=`dirname "$abspath"`
package_dir=$scripts_dir/..
applog_dir=$scripts_dir/log
logback_dir=$scripts_dir/../container/webapps/ROOT/WEB-INF/classes

#echo "Script Directory: $scripts_dir";

echo "#############################Starting Auto Configuration#############################"

echo "#############################STEP1: Check whether the auto configuration is activated#############################"
echo "#############################RESULT: continue the script or return#############################"
#The whole section is judging whether the auto configuration is activated

echo "Check if the autoConfig folder exists ";
if [ -d $scripts_dir/autoConfig ];then

	echo "Check if the setting file exists";	
	if [ -f $scripts_dir/setting ];then

		echo "Check if the autoConfig is activated";	
		if [ `cat "$scripts_dir/setting" | sed 's/#.*//g' | grep "TRUE"` ] || [ `cat "$scripts_dir/setting" | sed 's/#.*//g' |  grep "true"` ] || [ `cat "$scripts_dir/setting" | sed 's/#.*//g' | grep "True"` ]; then
			echo "Auto Configuration is activated"
		else	
			echo "Auto Configuration is not activated"			
			exit 0;
		fi

	else
		echo "There is no setting file inside the scripts folder. Startup Continue"
		exit 0;
	fi

else
	echo "There is no autoConfig folder. Startup Continue"
	exit 0;
fi


echo "#############################STEP2: GET and Parse Metadata#############################"
echo "#############################RESULT: Pin Point Corresponding Config File#############################"
# if Config File Could not be found: echo and return

$scripts_dir/ParseMetadata.sh

#read the result from tmp file
if [ -f $scripts_dir/tmp ];then
	if [ `cat "$scripts_dir/tmp"` ];then
		echo "Configuration File Found"
		configFile=`cat "$scripts_dir/tmp"`
		echo "configFile=$configFile"
	else 
		echo "[ERROR] Could not find config file OR multiple config files were found"
		exit 1;
	fi
else
	echo "Metadata Parsing Error. Nothing was returned from ParseMetadata.sh"
	exit 1;
fi
#remove the tmp file
rm "$scripts_dir/tmp"


echo "#############################STEP3: Extract the ROOT.war in container/webapp#############################"
echo "#############################RESULT: ROOT.war is extracted to container/webapp/ROOT folder"
#remove the previous ROOT folder and create an empty one
rm -rf $package_dir/container/webapps/ROOT
mkdir $package_dir/container/webapps/ROOT

#extract from war file 
cd $package_dir/container/webapps/ROOT
$package_dir/java/bin/jar -xvf ../ROOT.war

echo "##Remove all the "ctrl+v ctrl+M" from xml files"
perl -pi -e 's/
//g' `find . -name "*.xml"`
cd -

mv -f $logback_dir/logback.xml $logback_dir/logback.xml.bak
cat $logback_dir/logback.xml.bak | sed -e 's@<property name="logFile" value="cms.log"/>@<property name="logFile" value="'$applog_dir'/cms.log"/>@' >$logback_dir/logback.xml
touch $logback_dir/logback.xml -r $logback_dir/logback.xml.bak

echo "#############################STEP4: Parse the Config File and perform search and switch#############################"
echo "#############################RESULT: All the variables with $ sign is switched to the actual value#############################"
$scripts_dir/ParseConfigFile.sh $scripts_dir/autoConfig/$configFile

exit 0;


