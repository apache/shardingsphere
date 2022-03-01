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

package org.apache.shardingsphere.mode.repository.standalone.h2;

import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepository;
import java.util.List;

/**
 * H2 repository.
 */
// TODO Use the built-in h2 database to implement all of the following methods
public final class H2Repository implements StandalonePersistRepository {
    
    @Override
    public String get(final String key) {
        return null;
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return null;
    }
    
    @Override
    public void persist(final String key, final String value) {
        
    }
    
    @Override
    public void delete(final String key) {
        
    }
    
    @Override
    public void close() {
        
    }
    
    @Override
    public String getType() {
        return "H2";
    }
}
