#!/bin/sh

JAVA=/opt/java/bin/java
LOGFILE=/home/keonn/testEmbedded/log.log

$JAVA -jar /home/keonn/testEmbedded/TestEmbedded.jar -h localhost >> $LOGFILE 2>&1 &
echo $! > /var/run/embedded.pid