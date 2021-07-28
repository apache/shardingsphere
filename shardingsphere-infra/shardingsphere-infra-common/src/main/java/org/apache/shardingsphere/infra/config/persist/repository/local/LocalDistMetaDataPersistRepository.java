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

package org.apache.shardingsphere.infra.config.persist.repository.local;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.persist.repository.DistMetaDataPersistRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Local dist meta data persist repository.
 */
// TODO finish me
public final class LocalDistMetaDataPersistRepository implements DistMetaDataPersistRepository {
    
    private String path;
    
    @Override
    public String get(final String key) {
        return "";
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return Collections.emptyList();
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
        return "Local";
    }
    
    @Override
    public void setProps(final Properties props) {
        LocalRepositoryProperties localRepositoryProperties = new LocalRepositoryProperties(props);
        path = Optional.ofNullable(Strings.emptyToNull(localRepositoryProperties.getValue(LocalRepositoryPropertyKey.PATH)))
                .orElse(System.getProperty("user.home"));
    }
}
