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

package org.apache.shardingsphere.shadow.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.converter.UniqueRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.converter.NamedRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.converter.RuleRootNodePath;

/**
 * Shadow node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowNodeConverter {
    
    private static final RuleRootNodePath ROOT_NODE_CONVERTER = new RuleRootNodePath("shadow");
    
    private static final NamedRuleItemNodePath DATA_SOURCE_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "data_sources");
    
    private static final NamedRuleItemNodePath TABLE_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "tables");
    
    private static final NamedRuleItemNodePath ALGORITHM_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "algorithms");
    
    private static final UniqueRuleItemNodePath DEFAULT_ALGORITHM_NAME_NODE_CONVERTER = new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, "default_algorithm_name");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodePath getRuleRootNodeConverter() {
        return ROOT_NODE_CONVERTER;
    }
    
    /**
     * Get data source node converter.
     *
     * @return data source node converter
     */
    public static NamedRuleItemNodePath getDataSourceNodeConvertor() {
        return DATA_SOURCE_NODE_CONVERTER;
    }
    
    /**
     * Get table node converter.
     *
     * @return table node converter
     */
    public static NamedRuleItemNodePath getTableNodeConverter() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get algorithm node converter.
     *
     * @return algorithm node converter
     */
    public static NamedRuleItemNodePath getAlgorithmNodeConverter() {
        return ALGORITHM_NODE_CONVERTER;
    }
    
    /**
     * Get default algorithm name node converter.
     *
     * @return default algorithm name node converter
     */
    public static UniqueRuleItemNodePath getDefaultAlgorithmNameNodeConverter() {
        return DEFAULT_ALGORITHM_NAME_NODE_CONVERTER;
    }
}
