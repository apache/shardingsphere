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
import org.apache.shardingsphere.infra.metadata.converter.RuleItemNodeConverter;
import org.apache.shardingsphere.infra.metadata.converter.RuleRootNodeConverter;

/**
 * Readwrite-splitting node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingNodeConverter {
    
    private static final RuleRootNodeConverter ROOT_NODE_CONVERTER = new RuleRootNodeConverter("readwrite_splitting");
    
    private static final RuleItemNodeConverter DATA_SOURCE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "data_sources");
    
    private static final RuleItemNodeConverter LOAD_BALANCER_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "load_balancers");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodeConverter getRuleRootNodeConverter() {
        return ROOT_NODE_CONVERTER;
    }
    
    /**
     * Get data source node converter.
     *
     * @return data source node converter
     */
    public static RuleItemNodeConverter getDataSourceNodeConvertor() {
        return DATA_SOURCE_NODE_CONVERTER;
    }
    
    /**
     * Get load balancer node converter.
     *
     * @return load balancer node converter
     */
    public static RuleItemNodeConverter getLoadBalancerNodeConverter() {
        return LOAD_BALANCER_NODE_CONVERTER;
    }
}
