#!/usr/bin/env bash

#set -x

# Reinit the environnement
docker rm -f wildfly10

# Run a new container tomcat
docker run -d -P --name wildfly10 cloudunit/wildfly-10

# Check throug the custom script if applications is right deployed
docker exec -it wildfly10 sh /opt/cloudunit/scripts/status.sh

# Add custom credential users
docker exec -it wildfly10 sh /opt/cloudunit/scripts/init.sh johndoe abc2016

# Stop the container
docker stop wildfly10

# Start the container
docker start wildfly10

# Check throug the custom script if applications is right deployed
docker exec -it wildfly10 sh /opt/cloudunit/scripts/status.sh

# Get the mapped port for tcp/8080
MAPPED_PORT_8080=`docker inspect --format '{{ (index (index .NetworkSettings.Ports "8080/tcp") 0).HostPort }}' wildfly10`
MAPPED_PORT_9990=`docker inspect --format '{{ (index (index .NetworkSettings.Ports "9990/tcp") 0).HostPort }}' wildfly10`

echo "Port HTTP 8080 : " + $MAPPED_PORT_8080
echo "Port Console 9990 : " + $MAPPED_PORT_9990

docker exec -it wildfly10 touch /opt/jboss/wildfly/test.txt

docker stop wildfly10
docker start wildfly10

docker exec -it wildfly10 ls /opt/jboss/wildfly/test.txt

docker exec -it wildfly10 sh /opt/cloudunit/scripts/add_env.sh -Dnicolas=muller

docker stop wildfly10
docker start wildfly10


echo "****************************************"
echo "          SUCCESS                       "
echo "****************************************"
