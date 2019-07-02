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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert;

import lombok.Getter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Insert columns for sharding.
 *
 * @author zhangliang
 */
public final class ShardingInsertColumns {
    
    private final String generateKeyColumnName;
    
    private final Collection<String> assistedQueryColumnNames;
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public ShardingInsertColumns(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        generateKeyColumnName = shardingRule.findGenerateKeyColumnName(tableName).orNull();
        assistedQueryColumnNames = shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(tableName);
        regularColumnNames = insertStatement.getColumnNames().isEmpty() ? getRegularColumnNamesFromMetaData(shardingTableMetaData, tableName) : getRegularColumnNamesFromSQLStatement(insertStatement);
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(final ShardingTableMetaData shardingTableMetaData, final String tableName) {
        Collection<String> allColumnNames = shardingTableMetaData.getAllColumnNames(tableName);
        Collection<String> result = new LinkedHashSet<>(allColumnNames.size() - assistedQueryColumnNames.size());
        for (String each : allColumnNames) {
            if (!assistedQueryColumnNames.contains(each)) {
                result.add(each);
            }
        }
        if (null != generateKeyColumnName) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private Collection<String> getRegularColumnNamesFromSQLStatement(final InsertStatement insertStatement) {
        Collection<String> result = new LinkedHashSet<>(insertStatement.getColumnNames());
        if (null != generateKeyColumnName) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    /**
     * Get all column names.
     * 
     * <p>
     *     Include regular column names and derived column names with same sequence with insert values.
     * </p>
     * 
     * @return all column names
     */
    public Collection<String> getAllColumnNames() {
        Collection<String> result = new LinkedHashSet<>(regularColumnNames.size() + assistedQueryColumnNames.size() + 1);
        result.addAll(regularColumnNames);
        if (null != generateKeyColumnName) {
            result.add(generateKeyColumnName);
        }
        result.addAll(assistedQueryColumnNames);
        return result;
    }
}
