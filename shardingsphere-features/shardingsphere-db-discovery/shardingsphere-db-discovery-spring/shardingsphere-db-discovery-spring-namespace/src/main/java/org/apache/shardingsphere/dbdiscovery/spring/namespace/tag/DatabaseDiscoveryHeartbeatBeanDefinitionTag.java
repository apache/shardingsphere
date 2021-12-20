package org.apache.shardingsphere.dbdiscovery.spring.namespace.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Database discovery heartbeat bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DatabaseDiscoveryHeartbeatBeanDefinitionTag {
    
    public static final String ROOT_TAG = "discovery-heart-beat";
    
    public static final String HEARTBEAT_ID_ATTRIBUTE = "id";
    
    public static final String PROPS_TAG = "props";
}
