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
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Repository tuple persist service.
 */
@RequiredArgsConstructor
public final class RepositoryTuplePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Load repository tuples.
     *
     * @param rootNode root node
     * @return loaded repository tuples
     */
    public Collection<RepositoryTuple> load(final String rootNode) {
        return loadNodes(rootNode).stream().filter(VersionNodePath::isActiveVersionPath).map(this::createRepositoryTuple).collect(Collectors.toList());
    }
    
    private Collection<String> loadNodes(final String rootNode) {
        Collection<String> result = new LinkedHashSet<>();
        loadNodes(rootNode, result);
        return 1 == result.size() ? Collections.emptyList() : result;
    }
    
    private void loadNodes(final String toBeLoadedNode, final Collection<String> loadedNodes) {
        loadedNodes.add(toBeLoadedNode);
        for (String each : repository.getChildrenKeys(toBeLoadedNode)) {
            loadNodes(String.join("/", toBeLoadedNode, each), loadedNodes);
        }
    }
    
    private RepositoryTuple createRepositoryTuple(final String activeVersionPath) {
        String activeVersionKey = VersionNodePath.getVersionPath(activeVersionPath, Integer.parseInt(repository.query(activeVersionPath)));
        return new RepositoryTuple(activeVersionKey, repository.query(activeVersionKey));
    }
}
