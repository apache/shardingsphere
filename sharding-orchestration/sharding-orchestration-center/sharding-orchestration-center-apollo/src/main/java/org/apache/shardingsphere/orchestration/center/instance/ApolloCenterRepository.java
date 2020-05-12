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

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.instance.wrapper.ApolloConfigWrapper;
import org.apache.shardingsphere.orchestration.center.instance.wrapper.ApolloOpenApiWrapper;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;
import org.apache.shardingsphere.orchestration.center.util.ConfigKeyUtils;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Config center for Apollo.
 */
@Slf4j
public final class ApolloCenterRepository implements ConfigCenterRepository {
    
    private final Map<String, DataChangedEventListener> caches = new HashMap<>();
    
    private ApolloConfigWrapper configWrapper;
    
    private ApolloOpenApiWrapper openApiWrapper;
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public void init(final CenterConfiguration config) {
        ApolloProperties apolloProperties = new ApolloProperties(properties);
        configWrapper = new ApolloConfigWrapper(config, apolloProperties);
        openApiWrapper = new ApolloOpenApiWrapper(config, apolloProperties);
    }
    
    @Override
    public String get(final String key) {
        String value = configWrapper.getProperty(ConfigKeyUtils.pathToKey(key));
        return Strings.isNullOrEmpty(value) ? openApiWrapper.getValue(ConfigKeyUtils.pathToKey(key)) : value;
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return null;
    }
    
    @Override
    public void persist(final String key, final String value) {
        openApiWrapper.persist(ConfigKeyUtils.pathToKey(key), value);
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        String apolloKey = ConfigKeyUtils.pathToKey(key);
        caches.put(apolloKey, dataChangedEventListener);
        ConfigChangeListener listener = changeEvent -> {
            for (String changeKey : changeEvent.changedKeys()) {
                ConfigChange change = changeEvent.getChange(changeKey);
                DataChangedEvent.ChangedType changedType = getChangedType(change.getChangeType());
                if (DataChangedEvent.ChangedType.IGNORED == changedType) {
                    continue;
                }
                if (caches.get(changeKey) == null) {
                    continue;
                }
                caches.get(changeKey).onChange(new DataChangedEvent(ConfigKeyUtils.keyToPath(changeKey), change.getNewValue(), changedType));
            }
        };
        configWrapper.addChangeListener(listener, Collections.singleton(apolloKey), Collections.singleton(apolloKey));
    }
    
    @Override
    public void delete(final String key) {
        openApiWrapper.remove(ConfigKeyUtils.pathToKey(key));
    }
    
    private DataChangedEvent.ChangedType getChangedType(final PropertyChangeType changeType) {
        switch (changeType) {
            case ADDED:
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
