FROM java:7
MAINTAINER caohao "caohaoch@gmail.com"

ADD sharding-proxy-3.0.0.M1-SNAPSHOT.tar.gz /
RUN mv /sharding-proxy-3.0.0.M1-SNAPSHOT /opt/sharding-proxy
EXPOSE $PORT
ENTRYPOINT /opt/sharding-proxy/bin/start.sh $PORT && tail -f /opt/sharding-proxy/logs/stdout.log