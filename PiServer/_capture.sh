#!/bin/bash

DATE=$(date +"%Y-%m-%d_%H%M")

raspistill -vf -hf -o /home/pi/Cam/rasp/$DATE.jpg
# sshpass -p 'to' scp /home/pi/Cam/*.jpg  Thomas@192.168.20.11:Desktop/rasp
SOURCE="/home/pi/Cam/rasp/$DATE.jpg"
TARGET="/home/pi/Cam/rasp/"$DATE"_s.jpg"
convert $SOURCE -rotate 180 -resize 1024 -fill white -pointsize 20 -annotate 0x0+20+720 "$DATE" $TARGET
/usr/bin/wput -B  ftp://ftp1086455-thomas:wirsing01@wirsing.at/rasp/img/ -nc -q --basename=/home/pi/Cam/rasp/ $TARGET


