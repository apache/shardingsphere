package org.apache.shardingsphere.orchestration.center.instance;

import org.apache.shardingsphere.underlying.common.constant.properties.TypedProperties;

import java.util.Properties;

/**
 * Nacos properties.
 *
 * @author dongzonglei
 */
public final class NacosProperties extends TypedProperties<NacosPropertiesEnum> {
    
    public NacosProperties(final Properties props) {
        super(NacosPropertiesEnum.class, props);
    }
}
