package org.apache.shardingsphere.orchestration.center.instance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.constant.properties.TypedPropertiesKey;

/**
 * Nacos properties enum.
 *
 * @author dongzonglei
 */
@RequiredArgsConstructor
@Getter
public enum NacosPropertiesEnum implements TypedPropertiesKey {
    
    GROUP("group", "SHARDING_SPHERE_DEFAULT_GROUP", String.class),
    
    TIMEOUT("timeout", String.valueOf(3000), long.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
