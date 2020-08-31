/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.governance.repository.apollo;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.properties.TypedPropertyKey;

/**
 * Typed property key of Apollo.
 */
@RequiredArgsConstructor
@Getter
public enum ApolloPropertyKey implements TypedPropertyKey {
    
    /**
     * Apollo config client app id param.
     */
    APP_ID("appId", "APOLLO_SHARDINGSPHERE", String.class),
    
    /**
     * Apollo system environment param.
     */
    ENV("env", "DEV", String.class),
    
    /**
     * Apollo system cluster name.
     */
    CLUSTER_NAME("clusterName", ConfigConsts.CLUSTER_NAME_DEFAULT, String.class),
    
    /**
     * The administrator's name within the right of modify data for apollo open api client.
     */
    ADMINISTRATOR("administrator", "", String.class),
    
    /**
     * The token value for apollo open api client.
     */
    TOKEN("token", "", String.class),
    
    /**
     * The portal url for apollo open api client.
     */
    PORTAL_URL("portalUrl", "", String.class),
    
    /**
     * The connection timeout value for apollo open api client.
     */
    CONNECT_TIMEOUT("connectTimeout", String.valueOf(ApolloOpenApiConstants.DEFAULT_CONNECT_TIMEOUT), int.class),
    
    /**
     * The read timeout value for apollo open api client.
     */
    READ_TIMEOUT("readTimeout", String.valueOf(ApolloOpenApiConstants.DEFAULT_READ_TIMEOUT), int.class);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
}
