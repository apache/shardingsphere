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

package org.apache.shardingsphere.scaling.core.fixture;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Getter
@Setter
public final class FixtureRegistryRepository implements RegistryRepository, ConfigurationRepository {
    
    private static final Node REGISTRY_DATA = new Node();
    
    private Properties props = new Properties();
    
    @Override
    public void init(final String name, final GovernanceCenterConfiguration config) {
    }
    
    @Override
    public String get(final String key) {
        String[] paths = key.split("/");
        Node temp = REGISTRY_DATA;
        for (String path : paths) {
            if (null == temp) {
                return null;
            }
            temp = temp.get(path);
        }
        return null == temp ? null : temp.getValue();
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        String[] paths = key.split("/");
        Node temp = REGISTRY_DATA;
        for (String path : paths) {
            if (null == temp) {
                return Collections.emptyList();
            }
            temp = temp.get(path);
        }
        return null == temp ? Collections.emptyList() : new ArrayList<>(temp.getChildren().keySet());
    }
    
    @Override
    public void persist(final String key, final String value) {
        String[] paths = key.split("/");
        Node temp = REGISTRY_DATA;
        for (int i = 0; i < paths.length; i++) {
            if (i != paths.length - 1) {
                temp.add(paths[i]);
            } else {
                temp.put(paths[i], value);
            }
            temp = temp.get(paths[i]);
        }
    }
    
    @Override
    public void persistEphemeral(final String key, final String value) {
        persist(key, value);
    }
    
    @Override
    public void delete(final String key) {
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
    }
    
    @Override
    public void close() {
        REGISTRY_DATA.clear();
    }
    
    @Override
    public String getType() {
        return "REG_FIXTURE";
    }
    
    @Getter
    @Setter
    private static class Node {
        
        private String value;
        
        private Map<String, Node> children = Maps.newHashMap();
        
        public void add(final String key) {
            if (!children.containsKey(key)) {
                children.put(key, new Node());
            }
        }
        
        public void put(final String key, final String value) {
            if (!children.containsKey(key)) {
                children.put(key, new Node());
            }
            children.get(key).setValue(value);
        }
        
        public Node get(final String key) {
            return children.get(key);
        }
        
        public void clear() {
            value = null;
            children.clear();
        }
    }
}
