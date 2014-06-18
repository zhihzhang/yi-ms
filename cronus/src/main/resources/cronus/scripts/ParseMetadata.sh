#!/bin/bash
# 
# Use This Script for Auto Configuration
#
# author: mixia@ebay.com or Minhan.Xia@gmail.com

#script path
abspath=$(cd ${0%/*} && echo $PWD/${0##*/})
scripts_dir=`dirname "$abspath"`
package_dir=$scripts_dir/..
container_dir=$package_dir/container
java_dir=$package_dir/java
#echo "Script Directory: $scripts_dir";

#############################config files Path
configFilesPath="$scripts_dir/autoConfig"

#############################MetaData Path
metaDataPath=$scripts_dir/../../../../../.metadata.json

##########Change TO:CRONUSAPP_HOME

#<DEBUG>:
#metaDataPath="./res.json"
#configFilesPath="../autoConfig"
#echo "Metadata Directory: $metaDataPath";
#echo "Config Files Directory: $configFilesPath";
#</DEBUG>

#############################STEP1: Check override property file#############################
#############################RESULT: Find Out the specified config file and exit#############################
echo "LOG: Retrieving Override Service Property from metadata"
#previous parsing Solution:
#Override_Service_Property=`grep -o '"Override_Service_Property": ".*"' $metaDataPath | sed 's/"Override_Service_Property": "\(.*\)"/\1/g'`
#Override_Service_Property=`cat $metaDataPath | awk -F"[,:]" '{for(i=1;i<=NF;i++){if($i~/Override_Service_Property\042/){print $(i+1)} } }' | sed 's/ "\(.*\)"/\1/g'`

#final solution:
Override_Service_Property=`cat $metaDataPath | python -mjson.tool | grep "Override_Service_Property" | sed 's/.*"Override_Service_Property": "\(.*\)",*/\1/g'`
echo "Override_Service_Property ===== $Override_Service_Property"

if [ $Override_Service_Property ]; then 
	configFile=`ls $configFilesPath | grep $Override_Service_Property`

	echo "configFile: $configFile"
	#output the configFile to a temp file so that autoconfig.sh can read from it
	echo $configFile > $scripts_dir/tmp
	exit 0;
fi


#############################STEP2: Parse the ClassOfService Field of Metadata#############################
#############################RESULT: Find Out whether there is a specified property file#############################
echo "LOG: Retrieving the Class of Service from metadata"
#classOfService=`grep -o '"classOfService": ".*"' $metaDataPath | sed 's/"classOfService": "\(.*\)"/\1/g'`
#classOfService=`cat $metaDataPath | awk -F"[,:]" '{for(i=1;i<=NF;i++){if($i~/classOfService\042/){print $(i+1)} } }' | sed 's/ "\(.*\)"/\1/g'`

classOfService=`cat $metaDataPath | python -mjson.tool | grep "classOfService" | sed 's/.*"classOfService": "\(.*\)",*/\1/g'`
echo "classOfService ===== $classOfService"

echo "LOG: check if the classOfService has the following key word and find out the configuration file"
#class of service = QA
if [ $classOfService = "qa" ] || [ $classOfService = "QA" ];
	then
	configFile=`ls $configFilesPath | grep qa`
	
	mv -f $container_dir/bin/catalina.sh $container_dir/bin/catalina.sh.bak
	cat $container_dir/bin/catalina.sh.bak | sed -e 's/CATALINA_OPTS="-server -Xms22g -Xmx22g -XX:NewSize=3g -XX:MaxNewSize=3g -XX:SurvivorRatio=4 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -verbose:gc -XX:+PrintGCDetails -XX:+DisableExplicitGC -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"/CATALINA_OPTS="-server -Xms4g -Xmx4g -XX:NewSize=512m -XX:MaxNewSize=512m -XX:SurvivorRatio=4 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -verbose:gc -XX:+PrintGCDetails -XX:+DisableExplicitGC -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"/' >$container_dir/bin/catalina.sh
	touch $container_dir/bin/catalina.sh -r $container_dir/bin/catalina.sh.bak
	chmod +x $container_dir/bin/catalina.sh

        mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.7=-Xms22g/wrapper.java.additional.7=-Xms4g/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.8=-Xmx22g/wrapper.java.additional.8=-Xmx4g/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.9=-XX:NewSize=3g/wrapper.java.additional.9=-XX:NewSize=512m/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.10=-XX:MaxNewSize=3g/wrapper.java.additional.10=-XX:MaxNewSize=512m/' >$package_dir/conf/wrapper.conf
	touch $package_dir/conf/wrapper.conf -r $package_dir/conf/wrapper.conf.bak

#class of service = Production
elif [ $classOfService = "Production" ] || [ $classOfService = "production" ] || [ $classOfService = "PRODUCTION" ];
	then 
	configFile=`ls $configFilesPath | grep prod`

#class of service = Stage
elif [ $classOfService = "Stag" ] || [ $classOfService = "stag" ] || [ $classOfService = "STAG" ];
	then 
	configFile=`ls $configFilesPath | grep stag`
	
	mv -f $container_dir/bin/catalina.sh $container_dir/bin/catalina.sh.bak
	cat $container_dir/bin/catalina.sh.bak | sed -e 's/CATALINA_OPTS="-server -Xms22g -Xmx22g -XX:NewSize=3g -XX:MaxNewSize=3g -XX:SurvivorRatio=4 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -verbose:gc -XX:+PrintGCDetails -XX:+DisableExplicitGC -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"/CATALINA_OPTS="-server -Xms4g -Xmx4g -XX:NewSize=512m -XX:MaxNewSize=512m -XX:SurvivorRatio=4 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -verbose:gc -XX:+PrintGCDetails -XX:+DisableExplicitGC -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"/' >$container_dir/bin/catalina.sh
	touch $container_dir/bin/catalina.sh -r $container_dir/bin/catalina.sh.bak
	chmod +x $container_dir/bin/catalina.sh

        mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.7=-Xms22g/wrapper.java.additional.7=-Xms4g/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.8=-Xmx22g/wrapper.java.additional.8=-Xmx4g/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.9=-XX:NewSize=3g/wrapper.java.additional.9=-XX:NewSize=512m/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.10=-XX:MaxNewSize=3g/wrapper.java.additional.10=-XX:MaxNewSize=512m/' >$package_dir/conf/wrapper.conf
	touch $package_dir/conf/wrapper.conf -r $package_dir/conf/wrapper.conf.bak

#class of service = Pre-Production
elif [ $classOfService = "Pre-Production" ] || [ $classOfService = "pre-production" ]  || [ $classOfService = "PRE-PRODUCTION" ];
	then 
	configFile=`ls $configFilesPath | grep prep`
	
	mv -f $container_dir/bin/catalina.sh $container_dir/bin/catalina.sh.bak
	cat $container_dir/bin/catalina.sh.bak | sed -e 's/CATALINA_OPTS="-server -Xms22g -Xmx22g -XX:NewSize=3g -XX:MaxNewSize=3g -XX:SurvivorRatio=4 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -verbose:gc -XX:+PrintGCDetails -XX:+DisableExplicitGC -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"/CATALINA_OPTS="-server -Xms4g -Xmx4g -XX:NewSize=512m -XX:MaxNewSize=512m -XX:SurvivorRatio=4 -XX:+CMSParallelRemarkEnabled -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution -verbose:gc -XX:+PrintGCDetails -XX:+DisableExplicitGC -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Dorg.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH=true"/' >$container_dir/bin/catalina.sh
	touch $container_dir/bin/catalina.sh -r $container_dir/bin/catalina.sh.bak
	chmod +x $container_dir/bin/catalina.sh

        mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.7=-Xms22g/wrapper.java.additional.7=-Xms4g/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.8=-Xmx22g/wrapper.java.additional.8=-Xmx4g/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.9=-XX:NewSize=3g/wrapper.java.additional.9=-XX:NewSize=512m/' >$package_dir/conf/wrapper.conf
	mv -f $package_dir/conf/wrapper.conf $package_dir/conf/wrapper.conf.bak
	cat $package_dir/conf/wrapper.conf.bak | sed -e 's/wrapper.java.additional.10=-XX:MaxNewSize=3g/wrapper.java.additional.10=-XX:MaxNewSize=512m/' >$package_dir/conf/wrapper.conf
	touch $package_dir/conf/wrapper.conf -r $package_dir/conf/wrapper.conf.bak

fi

mv -f $container_dir/bin/catalina.sh $container_dir/bin/catalina.sh.bak
cat $container_dir/bin/catalina.sh.bak | sed -e 's@JAVA_HOME="/usr/bin/java"@JAVA_HOME="'$java_dir'"@' >$container_dir/bin/catalina.sh
touch $container_dir/bin/catalina.sh -r $container_dir/bin/catalina.sh.bak
chmod +x $container_dir/bin/catalina.sh

echo "configFile: $configFile"

#output the configFile to a temp file so that autoconfig.sh can read from it
echo $configFile > $scripts_dir/tmp


