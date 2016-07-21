#!/bin/sh

cd `dirname $0`

COUNT=2

START_SPEED=1000

temp_count=COUNT
while [ $temp_count -gt 0 ]; do
        temp_speed=$START_SPEED
        while [ $temp_speed -gt 0 ]; do
                ./bootstrap.sh start
                let "temp_speed=temp_speed-1"
        done
        let "temp_count=temp_count-1"
        sleep 1
done

temp_flush=COUNT

while [ $temp_flush -gt 0 ];do
        pids=`ps -f | grep java | grep "$DEPLOY_DIR" | awk '{print $2}' | wc -l`
        if [ $pids -gt 0 ]; then
                echo "当前启动进程数："$pids
        fi
        sleep 2
        let "temp_flush=temp_flush-1"
done