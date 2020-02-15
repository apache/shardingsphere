package org.apache.shardingsphere.orchestration.center.instance;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.constant.properties.TypedPropertiesKey;

/**
 * Apollo properties enum.
 *
 * @author dongzonglei
 */
@RequiredArgsConstructor
@Getter
public enum ApolloPropertiesEnum implements TypedPropertiesKey {
    
    APP_ID("appId", "APOLLO_SHARDING_SPHERE", String.class),
    
    ENV("env", "DEV", String.class),
    
    CLUSTER_NAME("clusterName", ConfigConsts.CLUSTER_NAME_DEFAULT, String.class),
    
    ADMINISTRATOR("administrator", "", String.class),
    
    TOKEN("token", "", String.class),
    
    PORTAL_URL("portalUrl", "", String.class),
    
    CONNECT_TIMEOUT("connectTimeout", String.valueOf(ApolloOpenApiConstants.DEFAULT_CONNECT_TIMEOUT), int.class),
    
    READ_TIMEOUT("readTimeout", String.valueOf(ApolloOpenApiConstants.DEFAULT_READ_TIMEOUT), int.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
