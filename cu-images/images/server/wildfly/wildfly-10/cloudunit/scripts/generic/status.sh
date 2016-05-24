#!/usr/bin/env bash

count=0;
RETURN=1

## We could wait 10 minutes...
until [ "$RETURN" -eq "0" ] || [ $count -gt 60 ];
do
    echo -n "Waiting for WildFly 10 start with log trace"
    curl http://localhost:9990/console/App.html
    RETURN=$?
    sleep 1
done

echo "Server WildFly 10 is started"
exit $RETURN
