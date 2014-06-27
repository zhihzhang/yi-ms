#!/bin/bash
# 
# Use This Script for Auto Configuration
#
# author: mixia@ebay.com or Minhan.Xia@gmail.com

abspath=$(cd ${0%/*} && echo $PWD/${0##*/})
scripts_dir=`dirname "$abspath"`
package_dir=$scripts_dir/..

#configFile Input
configFile=$1;

#searchAndSwitch path
searchAndSwitchPATH=$package_dir/container/webapps/ROOT/;

function searchAndSwitch()
{
	#echo search and switch;
	#call the searchAndSwitch script
	#$1=key $2=value $3=searchAndSwitchPATH
	# must use double quete to pass string; otherwise when an empty string will casuse the reduction of arguments.
	$scripts_dir/searchAndSwitch.sh "$1" "$2" "$3"

	#DEBUG: for testing whether all the values have been replaced	
	#grep $1 -rl /media/sf_VM_Shared/war
}


echo 'LOG:filter out the space and comments and output to tmp file'
echo configfile=$configFile
#grep -v '#' $configFile | grep = >tmp
#grep = $configFile | sed 's/#.*//g' > tmp 
#grep = $configFile | sed 's/#.*//g' | grep = > $scripts_dir/tmp
grep = $configFile | sed 's/^\#.*//g' | grep = > $scripts_dir/tmp

#parse the tmp file line by line
#retrive the key value pair
#perform search and switch base on the key value pair
while read LINE
do 
	echo "$LINE"
	#OLD: BUG: key=`echo $LINE | sed 's/\(.*\)=\(.*\)/\1/g'`
	#OLD: BUG: value=`echo $LINE | sed 's/\(.*\)=\(.*\)/\2/g'`
	# 11 20 2012 get the string before the first equal sign: to handle multiple equal sign
	# 20130121: MUST have double quote in echo to handle special char: otherwise will fail special char such as cron expression 
	key=`echo "$LINE" | awk -F= '{print $1}'`
	# get all but the first column; from the 2nd column
	value=`echo "$LINE" | cut -d\=  -f2-`
	# need to check when $value is null: if it is null; parse an empty string
	if [ -z "$key" ]; then key=""; fi
	echo "key=$key"
	echo "value=$value"
	echo "searchPath=$searchAndSwitchPATH"
	echo 
	# must use double quete to pass string; otherwise when an empty string will casuse the reduction of arguments.
	searchAndSwitch "$key" "$value" "$searchAndSwitchPATH";
done < $scripts_dir/tmp


rm $scripts_dir/tmp




