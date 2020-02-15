package org.apache.shardingsphere.underlying.common.constant.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Orchestration properties enum.
 *
 * @author dongzonglei
 */
@RequiredArgsConstructor
@Getter
public enum OrchestrationPropertiesEnum implements TypedPropertiesKey {
    
    OVERWRITE("overwrite", String.valueOf(Boolean.FALSE), boolean.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
