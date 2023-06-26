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

package org.apache.shardingsphere.single.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.nodepath.item.UniqueRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleRootNodePath;

/**
 * Single node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleNodeConverter {
    
    private static final String TABLES_NODE = "tables";
    
    private static final RuleRootNodePath ROOT_NODE_CONVERTER = new RuleRootNodePath("single");
    
    private static final UniqueRuleItemNodePath TABLE_NODE_CONVERTER = new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, "tables");
    
    /**
     * Get table node converter.
     *
     * @return table node converter
     */
    public static UniqueRuleItemNodePath getTableNodeConverter() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get tables path.
     *
     * @return tables path
     */
    public static String getTablesPath() {
        return TABLES_NODE;
    }
}
