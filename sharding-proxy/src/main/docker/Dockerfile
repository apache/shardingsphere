FROM java:7
MAINTAINER caohao "caohaoch@gmail.com"

ADD sharding-proxy-4.0.0.M1-SNAPSHOT.tar.gz /
RUN mv /sharding-proxy-4.0.0.M1-SNAPSHOT /opt/sharding-proxy
ENTRYPOINT /opt/sharding-proxy/bin/start.sh $PORT && tail -f /opt/sharding-proxy/logs/stdout.log
