# Base Dockerfile for all TOMCAT IMAGES
FROM cloudunit/base-12.04

# AJOUT DE LA VERSION DE TOMCAT
RUN wget https://github.com/Treeptik/cloudunit/releases/download/1.0/tomcat-appconf7.tar.gz -O /tmp/tomcat-appconf7.tar.gz \
	&& tar -xvf /tmp/tomcat-appconf7.tar.gz -C /cloudunit && rm /tmp/tomcat-appconf7.tar.gz

# ajout du dossier lib-ext

RUN mkdir -p /cloudunit/appconf/lib-ext
RUN mkdir -p /cloudunit/appconf/scripts

ADD scripts/ /cloudunit/appconf/scripts
RUN chmod +x /cloudunit/appconf/scripts/*

LABEL upgrade="commit"

# EXPOSE PORT
EXPOSE 22

VOLUME /cloudunit/appconf/logs

ENTRYPOINT ["/cloudunit/scripts/start-service.sh"]

