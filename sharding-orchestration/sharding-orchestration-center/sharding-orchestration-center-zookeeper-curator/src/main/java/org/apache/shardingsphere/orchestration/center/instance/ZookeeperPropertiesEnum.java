package org.apache.shardingsphere.orchestration.center.instance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.constant.properties.TypedPropertiesKey;

/**
 * Zookeeper properties enum.
 *
 * @author dongzonglei
 */
@RequiredArgsConstructor
@Getter
public enum ZookeeperPropertiesEnum implements TypedPropertiesKey {
    
    RETRY_INTERVAL_MILLISECONDS("retryIntervalMilliseconds", String.valueOf(500), int.class),
    
    MAX_RETRIES("maxRetries", String.valueOf(3), int.class),
    
    TIME_TO_LIVE_SECONDS("timeToLiveSeconds", String.valueOf(60), int.class),
    
    OPERATION_TIMEOUT_MILLISECONDS("operationTimeoutMilliseconds", String.valueOf(500), int.class),
    
    DIGEST("digest", null, String.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
