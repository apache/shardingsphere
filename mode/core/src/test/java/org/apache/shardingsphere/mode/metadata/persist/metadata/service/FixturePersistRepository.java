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

package org.apache.shardingsphere.mode.metadata.persist.metadata.service;

import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class FixturePersistRepository implements PersistRepository {
    
    private final Map<String, String> storage = new LinkedHashMap<>();
    
    @Override
    public String query(final String key) {
        return storage.get(key);
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        Set<String> result = new LinkedHashSet<>();
        String prefix = key.endsWith(PATH_SEPARATOR) ? key : key + PATH_SEPARATOR;
        for (String each : storage.keySet()) {
            if (each.startsWith(prefix)) {
                String remaining = each.substring(prefix.length());
                int index = remaining.indexOf(PATH_SEPARATOR);
                result.add(-1 == index ? remaining : remaining.substring(0, index));
            }
        }
        return new ArrayList<>(result);
    }
    
    @Override
    public boolean isExisted(final String key) {
        return storage.containsKey(key) || storage.keySet().stream().anyMatch(each -> each.startsWith(key + PATH_SEPARATOR));
    }
    
    @Override
    public void persist(final String key, final String value) {
        storage.put(key, value);
    }
    
    @Override
    public void update(final String key, final String value) {
        storage.put(key, value);
    }
    
    @Override
    public void delete(final String key) {
        storage.keySet().removeIf(each -> each.equals(key) || each.startsWith(key + PATH_SEPARATOR));
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
