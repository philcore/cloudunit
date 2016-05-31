#!/usr/bin/env bash

export CU_FILE=$1
export CU_JVM_ARGS=$2

# DELETE THE CLOUDUNIT CUSTOM BLOCK
sed -i '/#CLOUDUNIT_START/,/#CLOUDUNIT_END/d' $JBOSS_HOME/bin/standalone.conf

STRING_TO_INCLUDE_WITHOUT_INTERPRETATION='$JAVA_OPTS'

# ADD A CLOUDUNIT CUSTOM BLOCK WITH THE OPTION
echo "#CLOUDUNIT_START" >> $JBOSS_HOME/bin/standalone.conf
echo "JAVA_OPTS=\"$STRING_TO_INCLUDE_WITHOUT_INTERPRETATION $CU_JVM_ARGS \"" >> $JBOSS_HOME/bin/standalone.conf
echo "#CLOUDUNIT_END" >> $JBOSS_HOME/bin/standalone.conf

exit 0

