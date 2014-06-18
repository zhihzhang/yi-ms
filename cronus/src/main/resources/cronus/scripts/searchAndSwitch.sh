#!/bin/bash
# 
# Use This Script for Auto Configuration
#
# author: mixia@ebay.com or Minhan.Xia@gmail.com

#DEBUG:
#searchPath=/media/sf_VM_Shared/comparison/autoconfig/

#Production
searchPath=$3

#$1=key $2=value
echo "LOG: search for: $1 =========== switch to: $2"

#search the key $1 and replace it with the value $2
# escape  charactor with sed  &: the pattern matched;  
#20130121: Jeff To remove the windows ctrlM: !!!!TRICKY in new: CANNOT see the ctrlM in most editors! the NEW LINE is actually ctrlM: not a new line replaced: CANNOT COPY AND PASTE THIS CODE; always verify in VI   
#20130121: Jeff MUST have double quote in echo: otherwise will fail special char such as cron expression; 
old=$(echo "$1" |sed 's/[$/,.*?]/\\&/g')

new=$(echo "$2" |sed 's/[$/,.*?]/\\&/g' | sed 's/ //g')

echo "In searchAndSwitch.sh: with the escape char and replaced ctrlM: old=$old new=$new"

#There could not have any space in the path of the grep. so replace [space] with \[space]
#grep --exclude=*.{jar,class} '${'$old'}' -rl $searchPath: searvh for inlcuding '${'$old'}' string in all the paths: return file(path) list
# given the path list, to replace all space with escape char \
# with the replaced path list, as input for sed     sed ...  pathList
# 20130121: MUST have double quote in echo: otherwise will fail special char such as cron expression 
echo $1 ===== FOUND IN ===== `grep --exclude=*.{jar,class,ico,jpg,png} '${'"$old"'}' -rl $searchPath`
sed -i 's/${'"$old"'}/'"$new"'/g' `grep --exclude=*.{jar,class,ico,jpg,png} '${'"$old"'}' -rl $searchPath | sed 's/ /\\ /g'`
echo



