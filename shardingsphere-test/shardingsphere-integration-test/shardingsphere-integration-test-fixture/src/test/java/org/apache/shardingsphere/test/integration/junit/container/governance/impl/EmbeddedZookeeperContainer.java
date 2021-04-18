package org.apache.shardingsphere.test.integration.junit.container.governance.impl;

import lombok.SneakyThrows;
import org.apache.curator.test.TestingServer;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceCenterConfiguration;
import org.apache.shardingsphere.test.integration.junit.container.governance.ShardingSphereGovernanceContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

import java.util.Properties;

public class EmbeddedZookeeperContainer extends ShardingSphereGovernanceContainer {
    
    private TestingServer server;
    
    @SneakyThrows
    public EmbeddedZookeeperContainer(ParameterizedArray parameterizedArray) {
        super("zooKeeperServer", "zooKeeperServer", true, parameterizedArray);
        this.server = new TestingServer(true);
    }
    
    @SneakyThrows
    @Override
    public void start() {
        super.start();
        server.start();
    }
    
    @Override
    public boolean isHealthy() {
        return true;
    }
    
    @Override
    public String getServerLists() {
        return server.getConnectString();
    }
    
    @Override
    protected YamlGovernanceCenterConfiguration createGovernanceCenterConfiguration() {
        YamlGovernanceCenterConfiguration configuration = new YamlGovernanceCenterConfiguration();
        configuration.setServerLists(getServerLists());
        configuration.setType("zookeeper");
        Properties props = new Properties();
        props.setProperty("retryIntervalMilliseconds", "500");
        props.setProperty("timeToLiveSeconds", "60");
        props.setProperty("maxRetries", "3");
        props.setProperty("operationTimeoutMilliseconds", "500");
        configuration.setProps(props);
        return configuration;
    }
    
    @SneakyThrows
    @Override
    public void close() {
        server.stop();
    }
    
}
