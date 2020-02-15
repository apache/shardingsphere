package org.apache.shardingsphere.orchestration.center.instance;

import org.apache.shardingsphere.underlying.common.constant.properties.TypedProperties;

import java.util.Properties;

/**
 * Zookeeper properties.
 *
 * @author dongzonglei
 */
public final class ZookeeperProperties extends TypedProperties<ZookeeperPropertiesEnum> {
    
    public ZookeeperProperties(final Properties props) {
        super(ZookeeperPropertiesEnum.class, props);
    }
}
