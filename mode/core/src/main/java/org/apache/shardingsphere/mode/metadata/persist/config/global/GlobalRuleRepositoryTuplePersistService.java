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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Global rule repository tuple persist service.
 */
@RequiredArgsConstructor
public final class GlobalRuleRepositoryTuplePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Load rule contents.
     *
     * @return loaded content map, key is rule type
     */
    public Map<String, String> load() {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new GlobalRuleNodePath(null), false)).stream().collect(Collectors.toMap(each -> each, this::load));
    }
    
    /**
     * Load rule content.
     *
     * @param ruleType rule type
     * @return loaded content
     */
    public String load(final String ruleType) {
        return load(new VersionNodePath(new GlobalRuleNodePath(ruleType))).orElseThrow(() -> new IllegalStateException(String.format("Can not load rule type: %s", ruleType)));
    }
    
    private Optional<String> load(final VersionNodePath versionNodePath) {
        String activeVersionPath = versionNodePath.getActiveVersionPath();
        String version = repository.query(activeVersionPath);
        if (null == version) {
            return Optional.empty();
        }
        String versionPath = VersionNodePath.getVersionPath(activeVersionPath, Integer.parseInt(version));
        return Optional.of(repository.query(versionPath));
    }
}
