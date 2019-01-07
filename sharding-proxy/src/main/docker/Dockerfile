FROM java:7
MAINTAINER caohao "caohaoch@gmail.com"

ADD sharding-proxy-3.1.0.tar.gz /
RUN mv /sharding-proxy-3.1.0 /opt/sharding-proxy
ENTRYPOINT /opt/sharding-proxy/bin/start.sh $PORT && tail -f /opt/sharding-proxy/logs/stdout.log
