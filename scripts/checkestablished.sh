#!/bin/sh

cd `dirname $0`

#检查本机ip:port的连接数

if [ -n '$1' ];then
   echo "$1"
   echo  `netstat -anpt|grep $1 |grep ESTABLISHED |wc -l`
fi

