#!/bin/sh

cd `dirname $0`

#检查进程的文件句柄数
if [ -n '$1' ];then
  lsof -n|awk '{print $2}'|sort|uniq -c|sort -nr|grep $1
else
  lsof -n|awk '{print $2}'|sort|uniq -c|sort -nr|more
fi;