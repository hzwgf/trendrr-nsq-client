$ cat sysinfo.sh
#!/bin/bash

echo "cpu overview:"
echo ""
lscpu

echo ""
echo "every cpu info:"
echo ""
cat /proc/cpuinfo



echo ""
echo "memory info:"
echo ""
cat /proc/meminfo|grep 'MemTotal'


echo ""
echo "disk info:"
echo ""
lsblk


echo ""
echo "network card info:"
echo ""
ifconfig

echo ""
echo "eth0 info:"
echo ""
sudo -i ethtool eth0
