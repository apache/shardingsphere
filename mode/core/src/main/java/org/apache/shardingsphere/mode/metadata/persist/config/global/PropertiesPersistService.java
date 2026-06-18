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

package org.apache.shardingsphere.mode.metadata.persist.config.global;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.node.path.type.global.config.GlobalPropertiesNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Optional;
import java.util.Properties;

/**
 * Properties persist service.
 */
@RequiredArgsConstructor
public final class PropertiesPersistService {
    
    private final PersistRepository repository;
    
    private final VersionPersistService versionPersistService;
    
    /**
     * Load properties.
     *
     * @return properties
     */
    public Properties load() {
        return loadActiveVersion()
                .map(optional -> YamlEngine.unmarshal(repository.query(new VersionNodePath(new GlobalPropertiesNodePath()).getVersionPath(optional)), Properties.class))
                .orElse(new Properties());
    }
    
    private Optional<Integer> loadActiveVersion() {
        String value = repository.query(new VersionNodePath(new GlobalPropertiesNodePath()).getActiveVersionPath());
        return Strings.isNullOrEmpty(value) ? Optional.empty() : Optional.of(Integer.parseInt(value));
    }
    
    /**
     * Persist properties.
     *
     * @param props properties
     */
    public void persist(final Properties props) {
        VersionNodePath versionNodePath = new VersionNodePath(new GlobalPropertiesNodePath());
        versionPersistService.persist(versionNodePath, YamlEngine.marshal(props));
    }
}
