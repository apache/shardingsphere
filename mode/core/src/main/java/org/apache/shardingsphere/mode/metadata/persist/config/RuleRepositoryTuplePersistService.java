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

package org.apache.shardingsphere.mode.metadata.persist.config;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleRepositoryTuple;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

/**
 * Rule repository tuple persist service.
 */
@RequiredArgsConstructor
public final class RuleRepositoryTuplePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Load rule repository tuple.
     * 
     * @param activeVersionPath active version path
     * @return loaded tuple
     */
    public RuleRepositoryTuple load(final String activeVersionPath) {
        String versionPath = VersionNodePath.getVersionPath(activeVersionPath, Integer.parseInt(repository.query(activeVersionPath)));
        return new RuleRepositoryTuple(VersionNodePath.getOriginalPath(activeVersionPath), repository.query(versionPath));
    }
}
