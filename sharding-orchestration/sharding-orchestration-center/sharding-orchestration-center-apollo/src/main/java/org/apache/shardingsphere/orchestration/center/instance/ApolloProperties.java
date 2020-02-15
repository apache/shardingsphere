package org.apache.shardingsphere.orchestration.center.instance;

import org.apache.shardingsphere.underlying.common.constant.properties.TypedProperties;

import java.util.Properties;

/**
 * Apollo properties.
 *
 * @author dongzonglei
 */
public final class ApolloProperties extends TypedProperties<ApolloPropertiesEnum> {
    
    public ApolloProperties(final Properties props) {
        super(ApolloPropertiesEnum.class, props);
    }
}
