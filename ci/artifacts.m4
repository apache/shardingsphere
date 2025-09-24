group rshrd

artifact rshrd-jdbc RSHRD_VERSION
file RSHRD_VERSION/jdbc/target/apache-shardingsphere-RSHRD_VERSION-shardingsphere-jdbc-bin.tar.gz tar.gz
file RSHRD_VERSION/jdbc/target/apache-shardingsphere-RSHRD_VERSION-shardingsphere-jdbc-bin.tar.gz.sha512 txt
end

artifact rshrd-proxy RSHRD_VERSION
file RSHRD_VERSION/proxy/target/apache-shardingsphere-RSHRD_VERSION-shardingsphere-proxy-bin.tar.gz tar.gz
file RSHRD_VERSION/proxy/target/apache-shardingsphere-RSHRD_VERSION-shardingsphere-proxy-bin.tar.gz.sha512 txt
end

artifact rshrd-agent RSHRD_VERSION
file RSHRD_VERSION/agent/target/apache-shardingsphere-RSHRD_VERSION-shardingsphere-agent-bin.tar.gz tar.gz
file RSHRD_VERSION/agent/target/apache-shardingsphere-RSHRD_VERSION-shardingsphere-agent-bin.tar.gz.sha512 txt
end

artifact docker-containers-list RSHRD_VERSION
file ci/docker-containers-RSHRD_VERSION.txt
end