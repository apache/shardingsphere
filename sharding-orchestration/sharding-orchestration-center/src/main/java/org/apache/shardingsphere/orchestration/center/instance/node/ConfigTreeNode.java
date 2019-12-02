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

package org.apache.shardingsphere.orchestration.center.instance.node;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config Tree Node.
 *
 * @author dongzonglei
 * @author wangguangyuan
 * @author sunbufu
 */
@Getter
@Setter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigTreeNode {
    
    private static final String SHARDING_SPHERE_KEY_ROOT = "/";
    
    private static final String SHARDING_SPHERE_KEY_SEPARATOR = "/";
    
    private Map<String, Set<String>> childrenKeyMap = new ConcurrentHashMap<>();
    
    private final ConfigTreeNode parentNode;
    
    private final String absolutePath;
    
    private final Set<ConfigTreeNode> childrenNodes;
    
    /**
     * create new ConfigTreeNode.
     * 
     * @return ConfigTreeNode Root
     */
    public static ConfigTreeNode newBuilder() {
        return new ConfigTreeNode(null, "/", Sets.<ConfigTreeNode>newHashSet());
    }
    
    /**
     * init config tree.
     * 
     * @param instanceKeys instance Key set
     * @param keySeparator key separator
     * @return this
     */
    public ConfigTreeNode init(final Set<String> instanceKeys, final String keySeparator) {
        System.out.println(childrenKeyMap.size());
        initKeysRelationships(instanceKeys, keySeparator);
        init(this, SHARDING_SPHERE_KEY_ROOT);
        return this;
    }
    
    private void init(final ConfigTreeNode parentNode, final String shardingSphereKey) {
        Set<String> childrenKeys = childrenKeyMap.get(shardingSphereKey);
        if (childrenKeys == null || childrenKeys.isEmpty()) {
            return;
        }
        for (String each : childrenKeys) {
            ConfigTreeNode child = new ConfigTreeNode(parentNode, each, Sets.<ConfigTreeNode>newHashSet());
            parentNode.getChildrenNodes().add(child);
            init(child, each);
        }
    }
    
    private void initKeysRelationships(final Set<String> instanceKeys, final String keySeparator) {
        for (String each : instanceKeys) {
            initKeysRelationship(each, keySeparator);
        }
    }
    
    private void initKeysRelationship(final String instanceKey, final String keySeparator) {
        if (!instanceKey.contains(keySeparator)) {
            addRelationship(SHARDING_SPHERE_KEY_ROOT, Joiner.on("").join(SHARDING_SPHERE_KEY_ROOT, instanceKey));
            return;
        }
        String parentKey = "/";
        String shardingSphereKey = deConvertKey(instanceKey, keySeparator);
        for (int i = 1; i <= shardingSphereKey.lastIndexOf(SHARDING_SPHERE_KEY_SEPARATOR); i = shardingSphereKey.indexOf(SHARDING_SPHERE_KEY_SEPARATOR, i) + 1) {
            String childrenKey = shardingSphereKey.substring(0, shardingSphereKey.indexOf(SHARDING_SPHERE_KEY_SEPARATOR, i));
            addRelationship(parentKey, childrenKey);
            parentKey = childrenKey;
        }
        addRelationship(parentKey, shardingSphereKey);
    }
    
    private void addRelationship(final String parentKey, final String childrenKey) {
        Set<String> childrenKeys = childrenKeyMap.containsKey(parentKey) ? childrenKeyMap.get(parentKey) : new HashSet<String>();
        childrenKeys.add(childrenKey);
        childrenKeyMap.put(parentKey, childrenKeys);
    }
    
    private String deConvertKey(final String instanceKey, final String keySeparator) {
        return new StringBuilder(SHARDING_SPHERE_KEY_ROOT).append(instanceKey.replace(keySeparator, SHARDING_SPHERE_KEY_SEPARATOR)).toString();
    }
    
    /**
     * get children key set.
     * 
     * @param shardingSphereKey Sharding Sphere Key
     * @return children key set
     */
    public Set<String> getChildrenKeys(final String shardingSphereKey) {
        return childrenKeyMap.get(shardingSphereKey);
    }
    
    /**
     * refresh tree.
     * 
     * @param instanceKey new instance key
     * @param keySeparator key separator
     */
    public void refresh(final String instanceKey, final String keySeparator) {
        initKeysRelationship(instanceKey, keySeparator);
        this.getChildrenNodes().clear();
        init(this, SHARDING_SPHERE_KEY_ROOT);
    }
}
