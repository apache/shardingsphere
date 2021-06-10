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

package org.apache.shardingsphere.governance.core.registry.config.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.registry.config.service.GlobalRegistryService;
import org.apache.shardingsphere.governance.core.registry.config.node.GlobalNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Properties;

/**
 * Properties registry service.
 */
@RequiredArgsConstructor
public final class PropertiesRegistryService implements GlobalRegistryService<Properties> {
    
    private final RegistryCenterRepository repository;
    
    @Override
    public void persist(final Properties props, final boolean isOverwrite) {
        if (!props.isEmpty() && (isOverwrite || !isExisted())) {
            repository.persist(GlobalNode.getPropsPath(), YamlEngine.marshal(props));
        }
    }
    
    private boolean isExisted() {
        return !Strings.isNullOrEmpty(repository.get(GlobalNode.getPropsPath()));
    }
    
    @Override
    public Properties load() {
        return Strings.isNullOrEmpty(repository.get(GlobalNode.getPropsPath())) ? new Properties() : YamlEngine.unmarshal(repository.get(GlobalNode.getPropsPath()), Properties.class);
    }
}
