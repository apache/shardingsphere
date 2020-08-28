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

package org.apache.shardingsphere.orchestration.repository.apollo.wrapper;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import org.apache.shardingsphere.orchestration.repository.apollo.ApolloProperties;
import org.apache.shardingsphere.orchestration.repository.apollo.ApolloPropertyKey;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;

import java.util.Set;

/**
 * Apollo config wrapper.
 */
public final class ApolloConfigWrapper {
    
    private static final String APOLLO_KEY_APP_ID = "app.id";
    
    private static final String APOLLO_KEY_ENV = "env";
    
    private final Config apolloConfig;
    
    public ApolloConfigWrapper(final String namespace, final OrchestrationCenterConfiguration config, final ApolloProperties props) {
        String appId = props.getValue(ApolloPropertyKey.APP_ID);
        String env = props.getValue(ApolloPropertyKey.ENV);
        String clusterName = props.getValue(ApolloPropertyKey.CLUSTER_NAME);
        System.setProperty(APOLLO_KEY_APP_ID, appId);
        System.setProperty(APOLLO_KEY_ENV, env);
        System.setProperty(ConfigConsts.APOLLO_CLUSTER_KEY, clusterName);
        System.setProperty(ConfigConsts.APOLLO_META_KEY, config.getServerLists());
        apolloConfig = ConfigService.getConfig(namespace);
    }
    
    /**
     * Get property.
     * 
     * @param key key
     * @return value
     */
    public String getProperty(final String key) {
        return apolloConfig.getProperty(key, "");
    }
    
    /**
     * Add config change listener.
     * 
     * @param listener listener
     * @param interestedKeys monitor keys
     */
    public void addChangeListener(final ConfigChangeListener listener, final Set<String> interestedKeys) {
        apolloConfig.addChangeListener(listener, interestedKeys);
    }
    
    /**
     * Add config change listener.
     *
     * @param listener listener
     * @param interestedKeys monitor keys
     * @param interestedKeyPrefixes monitor key prefixes
     */
    public void addChangeListener(final ConfigChangeListener listener, final Set<String> interestedKeys, final Set<String> interestedKeyPrefixes) {
        apolloConfig.addChangeListener(listener, interestedKeys, interestedKeyPrefixes);
    }
}
