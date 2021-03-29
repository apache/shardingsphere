package org.apache.shardingsphere.test.integration.junit.container.governance;

import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

import javax.sql.DataSource;

public class ZookeeperContainer extends ShardingSphereContainer {
    
    public ZookeeperContainer(final ParameterizedArray parameterizedArray) {
        super("governance", "zookeeper:3.6.2", false, parameterizedArray);
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
