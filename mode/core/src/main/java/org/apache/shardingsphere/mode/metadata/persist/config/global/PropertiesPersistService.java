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
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.node.path.config.GlobalPropertiesNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collections;
import java.util.Properties;

/**
 * Properties persist service.
 */
@RequiredArgsConstructor
public final class PropertiesPersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    /**
     * Load properties.
     *
     * @return properties
     */
    public Properties load() {
        VersionNodePath versionNodePath = GlobalPropertiesNodePath.getVersionNodePath();
        Integer activeVersion = getActiveVersion(versionNodePath);
        String yamlContent = repository.query(versionNodePath.getVersionPath(null == activeVersion ? 0 : activeVersion));
        return Strings.isNullOrEmpty(yamlContent) ? new Properties() : YamlEngine.unmarshal(yamlContent, Properties.class);
    }
    
    /**
     * Persist properties.
     *
     * @param props properties
     */
    public void persist(final Properties props) {
        VersionNodePath versionNodePath = GlobalPropertiesNodePath.getVersionNodePath();
        int nextActiveVersion = metaDataVersionPersistService.getNextVersion(versionNodePath.getVersionsPath());
        repository.persist(versionNodePath.getVersionPath(nextActiveVersion), YamlEngine.marshal(props));
        if (null == getActiveVersion(versionNodePath)) {
            repository.persist(versionNodePath.getActiveVersionPath(), String.valueOf(MetaDataVersion.DEFAULT_VERSION));
        }
        metaDataVersionPersistService.switchActiveVersion(Collections.singleton(new MetaDataVersion(GlobalPropertiesNodePath.getRootPath(), getActiveVersion(versionNodePath), nextActiveVersion)));
    }
    
    private Integer getActiveVersion(final VersionNodePath versionNodePath) {
        String value = repository.query(versionNodePath.getActiveVersionPath());
        return Strings.isNullOrEmpty(value) ? null : Integer.parseInt(value);
    }
}
