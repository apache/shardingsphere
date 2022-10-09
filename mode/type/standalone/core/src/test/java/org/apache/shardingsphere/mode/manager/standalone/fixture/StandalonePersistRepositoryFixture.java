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

package org.apache.shardingsphere.mode.manager.standalone.fixture;

import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class StandalonePersistRepositoryFixture implements StandalonePersistRepository {
    
    private final Map<String, String> persistMap = new HashMap<>();
    
    @Override
    public void init(final Properties props) {
    }
    
    @Override
    public String get(final String key) {
        return persistMap.get(key);
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        List<String> result = new LinkedList<>();
        for (String each : persistMap.keySet()) {
            if (each.startsWith(key)) {
                String child = each.substring(key.length() + 1, each.indexOf('/', key.length() + 1));
                if (!result.contains(child)) {
                    result.add(child);
                }
            }
        }
        return result;
    }
    
    @Override
    public boolean isExisted(final String key) {
        return false;
    }
    
    @Override
    public void persist(final String key, final String value) {
        persistMap.put(key, value);
    }
    
    @Override
    public void update(final String key, final String value) {
        
    }
    
    @Override
    public void delete(final String key) {
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
