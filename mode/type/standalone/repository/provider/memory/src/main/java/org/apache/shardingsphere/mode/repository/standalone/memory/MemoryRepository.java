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

package org.apache.shardingsphere.mode.repository.standalone.memory;

import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Memory repository.
 */
public final class MemoryRepository implements StandalonePersistRepository {
    
    // CHECKSTYLE:OFF
    private final TreeMap<String, String> metaDataSortedMap = new TreeMap<>();
    // CHECKSTYLE:ON
    
    @Override
    public String query(final String key) {
        return metaDataSortedMap.get(key);
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        String searchPrefix;
        if (key.endsWith("/")) {
            searchPrefix = key;
        } else {
            searchPrefix = key + "/";
        }
        String endBound = key + Character.MAX_VALUE;
        return metaDataSortedMap.subMap(searchPrefix, true, endBound, false).keySet().stream()
                .map(each -> {
                    String remainder = each.substring(searchPrefix.length());
                    int nextSlashIndex = remainder.indexOf('/');
                    if (nextSlashIndex == -1) {
                        return remainder;
                    } else {
                        return remainder.substring(0, nextSlashIndex);
                    }
                })
                .filter(node -> !node.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean isExisted(final String key) {
        return metaDataSortedMap.containsKey(key);
    }
    
    @Override
    public void persist(final String key, final String value) {
        metaDataSortedMap.put(key, value);
    }
    
    @Override
    public void update(final String key, final String value) {
        if (metaDataSortedMap.containsKey(key)) {
            metaDataSortedMap.put(key, value);
        }
    }
    
    @Override
    public void delete(final String key) {
        metaDataSortedMap.remove(key);
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public Object getType() {
        return "Memory";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
