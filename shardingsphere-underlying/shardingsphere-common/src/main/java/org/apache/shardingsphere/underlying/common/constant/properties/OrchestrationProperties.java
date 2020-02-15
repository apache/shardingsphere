package org.apache.shardingsphere.underlying.common.constant.properties;

import java.util.Properties;

/**
 * Orchestration properties.
 *
 * @author dongzonglei
 */
public final class OrchestrationProperties extends TypedProperties<OrchestrationPropertiesEnum> {
    
    public OrchestrationProperties(final Properties props) {
        super(OrchestrationPropertiesEnum.class, props);
    }
}
