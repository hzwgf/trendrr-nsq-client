#查看磁盘io
iostat -d -k 1 10
#查看磁盘io,磁盘使用率繁忙标准：大于80%，io压力高标准：wa参数大于30%
iostat -d -x -k 1 10