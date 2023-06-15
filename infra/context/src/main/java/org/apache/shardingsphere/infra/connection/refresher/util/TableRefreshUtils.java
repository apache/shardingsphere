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

package org.apache.shardingsphere.infra.connection.refresher.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import java.util.Collection;
import java.util.Optional;

/**
 * Table refresh utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableRefreshUtils {
    
    /**
     * Get aggregated data source map.
     * 
     * @param databaseName database name
     * @param databaseType database type
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableName table name
     * @return aggregated data source map
     */
    public static boolean isRuleRefreshRequired(final String databaseName, final DatabaseType databaseType, final ShardingSphereRuleMetaData ruleMetaData,
                                                final String schemaName, final String tableName) {
        Optional<SingleRule> singleRule = ruleMetaData.findSingleRule(SingleRule.class);
        if (!singleRule.isPresent()) {
            return false;
        }
        Optional<DataNode> dataNode = singleRule.get().findTableDataNode(schemaName, tableName);
        if (!dataNode.isPresent()) {
            return false;
        }
        DataNode actualNode = dataNode.get();
        Collection<String> tablesConfig = SingleTableLoadUtils.splitTableLines(singleRule.get().getConfiguration().getTables());
        if (tablesConfig.contains(SingleTableConstants.ALL_TABLES) || tablesConfig.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return false;
        }
        Collection<DataNode> dataNods = SingleTableLoadUtils.convertToDataNodes(databaseName, databaseType, tablesConfig);
        for (DataNode each : dataNods) {
            if (each.equals(actualNode) || SingleTableConstants.ASTERISK.equals(each.getSchemaName())
                    || each.getDataSourceName().equals(actualNode.getDataSourceName()) && SingleTableConstants.ASTERISK.equals(each.getTableName())) {
                return false;
            }
        }
        return true;
    }
}
