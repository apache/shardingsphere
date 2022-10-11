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

package org.apache.shardingsphere.infra.rule.identifier.type;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * ShardingSphere rule which contains mutable data node.
 */
public interface MutableDataNodeRule extends ShardingSphereRule {
    
    /**
     * Add data node.
     *
     * @param dataSourceName data source name
     * @param schemaName schema name
     * @param tableName table name
     */
    void put(String dataSourceName, String schemaName, String tableName);
    
    /**
     * Remove data node.
     *
     * @param schemaName schema name
     * @param tableName table name
     */
    void remove(String schemaName, String tableName);
    
    /**
     * Remove data node.
     *
     * @param schemaNames schema name collection
     * @param tableName table name
     */
    void remove(Collection<String> schemaNames, String tableName);
    
    /**
     * Find single data node.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return single table data node
     */
    Optional<DataNode> findSingleTableDataNode(String schemaName, String tableName);
    
    /**
     * Reload single table rule.
     *
     * @param config rule configuration
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param builtRules built rules
     * @return single table rule
     */
    ShardingSphereRule reloadRule(RuleConfiguration config, String databaseName, Map<String, DataSource> dataSourceMap, Collection<ShardingSphereRule> builtRules);
}
