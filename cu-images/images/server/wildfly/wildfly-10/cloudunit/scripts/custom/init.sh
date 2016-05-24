#!/usr/bin/env bash

export CU_USER=$1
export CU_PASSWORD=$2
export CU_APP_NAME=$3

/opt/jboss/wildfly/bin/add-user.sh $CU_USER $CU_PASSWORD --silent

exit $?


