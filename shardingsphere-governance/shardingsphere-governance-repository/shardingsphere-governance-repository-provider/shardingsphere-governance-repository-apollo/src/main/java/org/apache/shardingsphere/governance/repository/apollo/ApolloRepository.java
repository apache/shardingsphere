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

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;
import org.apache.shardingsphere.governance.repository.apollo.wrapper.ApolloConfigWrapper;
import org.apache.shardingsphere.governance.repository.apollo.wrapper.ApolloOpenApiWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration repository for Apollo.
 */
@Slf4j
public final class ApolloRepository implements ConfigurationRepository {
    
    private static final String DOT_SEPARATOR = ".";
    
    private static final String PATH_SEPARATOR = "/";
    
    private final Map<String, DataChangedEventListener> caches = new HashMap<>();
    
    private ApolloConfigWrapper configWrapper;
    
    private ApolloOpenApiWrapper openApiWrapper;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Override
    public void init(final String name, final GovernanceCenterConfiguration config) {
        ApolloProperties apolloProperties = new ApolloProperties(props);
        configWrapper = new ApolloConfigWrapper(name, config, apolloProperties);
        openApiWrapper = new ApolloOpenApiWrapper(name, apolloProperties);
    }
    
    @Override
    public String get(final String key) {
        String value = configWrapper.getProperty(pathToKey(key));
        return Strings.isNullOrEmpty(value) ? openApiWrapper.getValue(pathToKey(key)) : value;
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return null;
    }
    
    @Override
    public void persist(final String key, final String value) {
        openApiWrapper.persist(pathToKey(key), value);
    }
    
    @Override
    public void delete(final String key) {
        openApiWrapper.remove(pathToKey(key));
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        String apolloKey = pathToKey(key);
        caches.put(apolloKey, dataChangedEventListener);
        ConfigChangeListener listener = changeEvent -> {
            for (String changeKey : changeEvent.changedKeys()) {
                ConfigChange change = changeEvent.getChange(changeKey);
                ChangedType changedType = getChangedType(change.getChangeType());
                if (ChangedType.IGNORED == changedType) {
                    continue;
                }
                if (!caches.containsKey(changeKey)) {
                    continue;
                }
                caches.get(changeKey).onChange(new DataChangedEvent(keyToPath(changeKey), change.getNewValue(), changedType));
            }
        };
        configWrapper.addChangeListener(listener, Collections.singleton(apolloKey), Collections.singleton(apolloKey));
    }
    
    private ChangedType getChangedType(final PropertyChangeType changeType) {
        switch (changeType) {
            case ADDED:
            case MODIFIED:
                return ChangedType.UPDATED;
            case DELETED:
                return ChangedType.DELETED;
            default:
                return ChangedType.IGNORED;
        }
    }
    
    private String pathToKey(final String path) {
        String key = path.replace(PATH_SEPARATOR, DOT_SEPARATOR);
        return key.substring(key.indexOf(DOT_SEPARATOR) + 1);
    }
    
    private String keyToPath(final String key) {
        return PATH_SEPARATOR + key.replace(DOT_SEPARATOR, PATH_SEPARATOR);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "Apollo";
    }
}
