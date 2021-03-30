package org.apache.shardingsphere.test.integration.junit.container.governance;

import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import javax.sql.DataSource;

public class ZookeeperContainer extends ShardingSphereContainer {
    
    public ZookeeperContainer(final ParameterizedArray parameterizedArray) {
        super("zookeeper", "zookeeper:3.6.2", false, parameterizedArray);
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*PrepRequestProcessor \\(sid:[0-9]+\\) started.*"));
    }
    
    /**
     * Get DataSource.
     *
     * @return DataSource
     */
    public DataSource getDataSource() {
        return null;
    }
}
