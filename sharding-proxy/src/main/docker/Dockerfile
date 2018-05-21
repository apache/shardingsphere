FROM java:7
MAINTAINER caohao "caohaoch@gmail.com"

ADD sharding-proxy-3.0.0.M1.tar.gz /
RUN mv /sharding-proxy-3.0.0.M1 /opt/sharding-proxy
ENTRYPOINT /opt/sharding-proxy/bin/start.sh $PORT && tail -f /opt/sharding-proxy/logs/stdout.log
