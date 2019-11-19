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

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.constant.ApolloOpenApiConstants;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

/**
 * Registry center for Apollo.
 *
 * @author dongzonglei
 */
@Slf4j
public final class ApolloInstance implements ConfigCenter {
    
    private static final String SHARDING_SPHERE_KEY_ROOT = "/";
    
    private static final String SHARDING_SPHERE_KEY_SEPARATOR = "/";
    
    private static final String APOLLO_KEY_SEPARATOR = ".";
    
    private String namespace;
    
    private String appId;
    
    private String env;
    
    private String clusterName;
    
    private String modifiyDataUserId;
    
    private Config apolloConfig;
    
    private ApolloOpenApiClient client;
    
    private Map<String, Set<String>> keyAndChildrenMap = new HashMap<>();
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public void init(final InstanceConfiguration config) {
        initApolloConfig(config);
        initApolloOpenApiClient();
        initKeysRelationship();
    }
    
    private void initApolloConfig(final InstanceConfiguration config) {
        namespace = config.getNamespace();
        appId = properties.getProperty("appId", "APOLLO_SHARDING_SPHERE");
        env = properties.getProperty("env", "DEV");
        clusterName = properties.getProperty("clusterName", ConfigConsts.CLUSTER_NAME_DEFAULT);
        System.setProperty("app.id", appId);
        System.setProperty("env", env);
        System.setProperty(ConfigConsts.APOLLO_CLUSTER_KEY, clusterName);
        System.setProperty(ConfigConsts.APOLLO_META_KEY, config.getServerLists());
        apolloConfig = ConfigService.getConfig(namespace);
    }
    
    private void initApolloOpenApiClient() {
        modifiyDataUserId = properties.getProperty("modifiyDataUserId");
        String apolloToken = properties.getProperty("token");
        String portalUrl = properties.getProperty("portalUrl");
        String connectTimeout = properties.getProperty("connectTimeout", Objects.toString(ApolloOpenApiConstants.DEFAULT_CONNECT_TIMEOUT));
        String readTimeout = properties.getProperty("readTimeout", Objects.toString(ApolloOpenApiConstants.DEFAULT_READ_TIMEOUT));
        client = ApolloOpenApiClient.newBuilder().withPortalUrl(portalUrl)
                .withConnectTimeout(Ints.tryParse(connectTimeout)).withReadTimeout(Ints.tryParse(readTimeout))
                .withToken(apolloToken).build();
    }
    
    private void initKeysRelationship() {
        List<OpenItemDTO> items = client.getNamespace(appId, env, clusterName, namespace).getItems();
        for (OpenItemDTO each : items) {
            if (!each.getKey().contains(APOLLO_KEY_SEPARATOR)) {
                addRelationship(SHARDING_SPHERE_KEY_ROOT, SHARDING_SPHERE_KEY_ROOT + each.getKey());
                continue;
            }
            initKeysRelationship(each.getKey());
        }
    }
    
    private void initKeysRelationship(final String apolloKey) {
        String parentKey = SHARDING_SPHERE_KEY_ROOT;
        String shardingSphereKey = deConvertKey(apolloKey);
        for (int i = 1; i <= shardingSphereKey.lastIndexOf(SHARDING_SPHERE_KEY_SEPARATOR); i = shardingSphereKey.indexOf(SHARDING_SPHERE_KEY_SEPARATOR, i) + 1) {
            String childrenKey = shardingSphereKey.substring(0, shardingSphereKey.indexOf(SHARDING_SPHERE_KEY_SEPARATOR, i));
            addRelationship(parentKey, childrenKey);
            parentKey = childrenKey;
        }
        addRelationship(parentKey, shardingSphereKey);
    }
    
    private void addRelationship(final String parentKey, final String childrenKey) {
        Set<String> childrenKeys = keyAndChildrenMap.containsKey(parentKey) ? keyAndChildrenMap.get(parentKey) : new HashSet<String>();
        childrenKeys.add(childrenKey);
        keyAndChildrenMap.put(parentKey, childrenKeys);
    }
    
    @Override
    public String get(final String key) {
        return apolloConfig.getProperty(convertKey(key), "");
    }
    
    private String convertKey(final String shardingSphereKey) {
        return shardingSphereKey.replace(SHARDING_SPHERE_KEY_SEPARATOR, APOLLO_KEY_SEPARATOR).substring(1);
    }
    
    private String deConvertKey(final String apolloKey) {
        return new StringBuilder(SHARDING_SPHERE_KEY_ROOT).append(apolloKey.replace(APOLLO_KEY_SEPARATOR, SHARDING_SPHERE_KEY_SEPARATOR)).toString();
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return new ArrayList<>(keyAndChildrenMap.get(key));
    }
    
    @Override
    public void persist(final String key, final String value) {
        String apolloKey = convertKey(key);
        updateKey(apolloKey, value);
        publishNamespace();
        if (!apolloKey.contains(".")) {
            addRelationship(SHARDING_SPHERE_KEY_ROOT, SHARDING_SPHERE_KEY_ROOT + apolloKey);
            return;
        }
        initKeysRelationship(apolloKey);
    }
    
    private void updateKey(final String key, final String value) {
        OpenItemDTO openItem = new OpenItemDTO();
        openItem.setKey(key);
        openItem.setValue(value);
        openItem.setComment("ShardingSphere create or update config");
        openItem.setDataChangeCreatedBy(modifiyDataUserId);
        client.createOrUpdateItem(appId, env, clusterName, namespace, openItem);
    }
    
    private void publishNamespace() {
        NamespaceReleaseDTO release = new NamespaceReleaseDTO();
        release.setReleaseTitle("ShardingSphere namespace release");
        release.setReleaseComment("ShardingSphere namespace release");
        release.setReleasedBy(modifiyDataUserId);
        release.setEmergencyPublish(true);
        client.publishNamespace(appId, env, clusterName, namespace, release);
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        apolloConfig.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(final ConfigChangeEvent changeEvent) {
                for (String key : changeEvent.changedKeys()) {
                    ConfigChange change = changeEvent.getChange(key);
                    DataChangedEvent.ChangedType changedType = getChangedType(change.getChangeType());
                    if (DataChangedEvent.ChangedType.IGNORED != changedType) {
                        dataChangedEventListener.onChange(new DataChangedEvent(deConvertKey(key), change.getNewValue(), changedType));
                    }
                }
            }
        }, Sets.newHashSet(key));
    }
    
    private DataChangedEvent.ChangedType getChangedType(final PropertyChangeType changeType) {
        switch (changeType) {
            case MODIFIED:
                return DataChangedEvent.ChangedType.UPDATED;
            case DELETED:
                return DataChangedEvent.ChangedType.DELETED;
            default:
                return DataChangedEvent.ChangedType.IGNORED;
        }
    }
    
    @Override
    public void close() {

    }
    
    @Override
    public String getType() {
        return "apollo";
    }
}
