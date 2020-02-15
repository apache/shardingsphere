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
