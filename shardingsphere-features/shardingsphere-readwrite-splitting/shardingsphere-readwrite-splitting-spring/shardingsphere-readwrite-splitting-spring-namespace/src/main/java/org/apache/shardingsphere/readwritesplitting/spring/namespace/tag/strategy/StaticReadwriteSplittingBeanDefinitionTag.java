package org.apache.shardingsphere.readwritesplitting.spring.namespace.tag.strategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Readwrite-splitting static bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StaticReadwriteSplittingBeanDefinitionTag {
    
    public static final String STATIC_STRATEGY_ROOT_TAG = "static-strategy";
    
    public static final String WRITE_DATA_SOURCE_NAME = "write-data-source-name";
    
    public static final String READ_DATA_SOURCE_NAMES = "read-data-source-names";
}
