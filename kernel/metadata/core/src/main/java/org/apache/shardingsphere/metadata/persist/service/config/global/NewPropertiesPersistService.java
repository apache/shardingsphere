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

package org.apache.shardingsphere.metadata.persist.service.config.global;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.metadata.persist.node.NewGlobalNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Properties;

/**
 * TODO Rename PropertiesPersistService when metadata structure adjustment completed. #25485
 * Properties persist service.
 */
@RequiredArgsConstructor
public final class NewPropertiesPersistService implements GlobalPersistService<Properties> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final Properties props) {
        if (Strings.isNullOrEmpty(repository.getDirectly(NewGlobalNode.getPropsActiveVersionNode()))) {
            repository.persist(NewGlobalNode.getPropsActiveVersionNode(), DEFAULT_VERSION);
        }
        repository.persist(NewGlobalNode.getPropsVersionNode(DEFAULT_VERSION), YamlEngine.marshal(props));
    }
    
    @Override
    public Properties load() {
        // TODO
        return new Properties();
    }
}
