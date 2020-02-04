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

package org.apache.shardingsphere.orchestration.center.instance.wrapper;

import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.google.common.primitives.Ints;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;

import java.util.Objects;
import java.util.Properties;

/**
 * Apollo open api client wrapper.
 *
 * @author dongzonglei
 */
public final class ApolloOpenApiWrapper {
    
    private ApolloOpenApiClient client;
    
    private String namespace;
    
    private String appId;
    
    private String env;
    
    private String clusterName;
    
    private String administrator;
    
    public ApolloOpenApiWrapper(final InstanceConfiguration config, final Properties properties) {
        namespace = config.getNamespace();
        appId = properties.getProperty("appId", "APOLLO_SHARDING_SPHERE");
        env = properties.getProperty("env", "DEV");
        clusterName = properties.getProperty("clusterName", ConfigConsts.CLUSTER_NAME_DEFAULT);
        administrator = properties.getProperty("administrator");
        String apolloToken = properties.getProperty("token");
        String portalUrl = properties.getProperty("portalUrl");
        Integer connectTimeout = Ints.tryParse(Objects.toString(properties.get("connectTimeout")));
        Integer readTimeout = Ints.tryParse(Objects.toString(properties.get("readTimeout")));
        client = ApolloOpenApiClient.newBuilder().withPortalUrl(portalUrl)
                .withConnectTimeout(connectTimeout == null ? ApolloOpenApiConstants.DEFAULT_CONNECT_TIMEOUT : connectTimeout)
                .withReadTimeout(readTimeout == null ? ApolloOpenApiConstants.DEFAULT_READ_TIMEOUT : readTimeout)
                .withToken(apolloToken).build();
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
