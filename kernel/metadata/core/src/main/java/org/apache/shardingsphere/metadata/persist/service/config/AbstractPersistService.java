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
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public abstract class AbstractPersistService {
    
    private static final String ACTIVE_VERSION_PATTERN = "/active_version$";
    
    private static final String ACTIVE_VERSION_PATH = "active_version";
    
    private static final String VERSIONS_PATH = "versions";
    
    private final PersistRepository repository;
    
    /**
     * Get data nodes.
     *
     * @param rootPath root path
     * @return yaml data nodes
     */
    public Collection<YamlDataNode> getDataNodes(final String rootPath) {
        Collection<YamlDataNode> result = new LinkedList<>();
        for (String each : getNodes(rootPath)) {
            Pattern pattern = Pattern.compile(ACTIVE_VERSION_PATTERN, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(each);
            if (matcher.find()) {
                String activeRuleKey = each.replace(ACTIVE_VERSION_PATH, VERSIONS_PATH) + "/" + getActiveVersion(each);
                result.add(new YamlDataNode(activeRuleKey, repository.getDirectly(activeRuleKey)));
            }
        }
        return result;
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
        List<String> childKeys = repository.getChildrenKeys(path);
        if (childKeys.isEmpty()) {
            return;
        }
        for (String each : childKeys) {
            getAllNodes(keys, getPath(path, each));
        }
    }
    
    private String getPath(final String path, final String childKey) {
        return String.join("/", path, childKey);
    }
    
    protected String getActiveVersion(final String key) {
        return repository.getDirectly(key);
    }
}
