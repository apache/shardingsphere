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

package org.apache.shardingsphere.metadata.persist.service.config;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractPersistService {
    
    private static final String ACTIVE_VERSION_PATTERN = "/active_version$";
    
    private static final String ACTIVE_VERSION_PATH = "active_version";
    
    private static final String VERSIONS_PATH = "versions";
    
    private final PersistRepository repository;
    
    protected final Collection<RepositoryTuple> getRepositoryTuples(final String rootPath) {
        Pattern pattern = Pattern.compile(ACTIVE_VERSION_PATTERN, Pattern.CASE_INSENSITIVE);
        return getNodes(rootPath).stream().filter(each -> pattern.matcher(each).find()).map(this::getRepositoryTuple).collect(Collectors.toList());
    }
    
    private Collection<String> getNodes(final String rootPath) {
        Collection<String> result = new LinkedHashSet<>();
        getAllNodes(result, rootPath);
        if (1 == result.size()) {
            return Collections.emptyList();
        }
        return result;
    }
    
    private void getAllNodes(final Collection<String> keys, final String path) {
        keys.add(path);
        for (String each : repository.getChildrenKeys(path)) {
            getAllNodes(keys, String.join("/", path, each));
        }
    }
    
    private RepositoryTuple getRepositoryTuple(final String node) {
        String activeRuleKey = node.replace(ACTIVE_VERSION_PATH, VERSIONS_PATH) + "/" + repository.getDirectly(node);
        return new RepositoryTuple(activeRuleKey, repository.getDirectly(activeRuleKey));
    }
}
