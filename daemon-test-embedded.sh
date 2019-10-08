#! /bin/bash 
#
# Copyright (c) 2016 Keonn technologies S.L.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
#


#=== usage ================================================================
# DESCRIPTION: Display usage information for this script.
#==========================================================================

### BEGIN INIT INFO
# Provides: TestEmbedded
# Required-Start:	$syslog $local_fs
# Required-Stop:	$syslog $local_fs
# Should-Start:
# Default-Start:	2 3 4 5
# Default-Stop:		0 1 6
# Short-Description: Keonn device firmware
### END INIT INFO


usage() {

	echo "Usage: daemon-test-embedded {start|stop|restart} options" >&2
}

start() {
	/home/keonn/testEmbedded/start-embedded-app.sh
	echo "[ OK ] Engine has started"
}


stop() {
	echo "Stopping $DESC" "$NAME"	

	if [ ! -z "$PIDFILE" ]; then
		
		echo "Sending SIGHUP to process: `cat $PIDFILE`"
		pkill -TERM -P `cat $PIDFILE`
			
		sleep 1
			
		i="0"
		# 20 x sleep 3 --> 1 minut max waiting
		while [ $i -lt 20 ]
		do
			if pkill -TERM -P -0 `cat $PIDFILE` ; then
				echo 'Killing failed. Waiting for the process to end...'
    				i=$[$i+1]
    			else 
    				break
			fi
			
			sleep 3
				
		done
			
		if pkill -TERM -P -0 `cat $PIDFILE` ; then
			echo 'Killing failed. Forcing shutdown..'
			pkill -TERM -P -9 `cat $PIDFILE`
			sleep 5
		fi
		
        else
        	echo "Kill failed: \$PIDFILE not set"
        fi

}

action="$1"
if [ "$action" == "" ]; then
	usage;
	exit 3;
fi

shift

NAME="embedded"
PIDFILE=/var/run/$NAME.pid

case "$action" in
  start)
        start
	;;

  stop)
        stop
        ;;
  restart)
	stop
	start
	;;
*)
	usage
	exit 1
	;;
esac

