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

package org.apache.shardingsphere.readwritesplitting.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.nodepath.item.NamedRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleRootNodePath;

/**
 * Readwrite-splitting node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingNodeConverter {
    
    private static final RuleRootNodePath ROOT_NODE_PATH = new RuleRootNodePath("readwrite_splitting");
    
    private static final NamedRuleItemNodePath DATA_SOURCE_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "data_sources");
    
    private static final NamedRuleItemNodePath LOAD_BALANCER_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "load_balancers");
    
    /**
     * Get rule root node path.
     *
     * @return rule root node path
     */
    public static RuleRootNodePath getRuleRootNodePath() {
        return ROOT_NODE_PATH;
    }
    
    /**
     * Get data source node path.
     *
     * @return data source node path
     */
    public static NamedRuleItemNodePath getDataSourceNodePath() {
        return DATA_SOURCE_NODE_PATH;
    }
    
    /**
     * Get load balancer node path.
     *
     * @return load balancer node path
     */
    public static NamedRuleItemNodePath getLoadBalancerNodePath() {
        return LOAD_BALANCER_NODE_PATH;
    }
}
