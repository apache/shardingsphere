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

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import org.apache.shardingsphere.orchestration.repository.apollo.ApolloProperties;
import org.apache.shardingsphere.orchestration.repository.apollo.ApolloPropertyKey;

/**
 * Apollo open API client wrapper.
 */
public final class ApolloOpenApiWrapper {
    
    private final String namespace;
    
    private final String appId;
    
    private final String env;
    
    private final String clusterName;
    
    private final String administrator;
    
    private final ApolloOpenApiClient client;
    
    public ApolloOpenApiWrapper(final String namespace, final ApolloProperties props) {
        this.namespace = namespace;
        appId = props.getValue(ApolloPropertyKey.APP_ID);
        env = props.getValue(ApolloPropertyKey.ENV);
        clusterName = props.getValue(ApolloPropertyKey.CLUSTER_NAME);
        administrator = props.getValue(ApolloPropertyKey.ADMINISTRATOR);
        String apolloToken = props.getValue(ApolloPropertyKey.TOKEN);
        String portalUrl = props.getValue(ApolloPropertyKey.PORTAL_URL);
        Integer connectTimeout = props.getValue(ApolloPropertyKey.CONNECT_TIMEOUT);
        Integer readTimeout = props.getValue(ApolloPropertyKey.READ_TIMEOUT);
        client = ApolloOpenApiClient.newBuilder().withPortalUrl(portalUrl).withConnectTimeout(connectTimeout).withReadTimeout(readTimeout).withToken(apolloToken).build();
    }
    
    /**
     * Get config value by key.
     * 
     * @param key key
     * @return value
     */
    public String getValue(final String key) {
        OpenItemDTO itemDTO = client.getItem(appId, env, clusterName, namespace, key);
        if (itemDTO == null) {
            return null;
        }
        return itemDTO.getValue();
    }
    
    /**
     * Persist config.
     * 
     * @param key key
     * @param value value
     */
    public void persist(final String key, final String value) {
        updateKey(key, value);
        publishNamespace();
    }
    
    /**
     * Remove config.
     *
     * @param key key
     */
    public void remove(final String key) {
        client.removeItem(appId, env, clusterName, namespace, key, administrator);
    }
    
    private void updateKey(final String key, final String value) {
        OpenItemDTO openItem = new OpenItemDTO();
        openItem.setKey(key);
        openItem.setValue(value);
        openItem.setComment("ShardingSphere create or update config");
        openItem.setDataChangeCreatedBy(administrator);
        client.createOrUpdateItem(appId, env, clusterName, namespace, openItem);
    }
    
    private void publishNamespace() {
        NamespaceReleaseDTO release = new NamespaceReleaseDTO();
        release.setReleaseTitle("ShardingSphere namespace release");
        release.setReleaseComment("ShardingSphere namespace release");
        release.setReleasedBy(administrator);
        release.setEmergencyPublish(true);
        client.publishNamespace(appId, env, clusterName, namespace, release);
    }
}
