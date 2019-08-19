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

package org.apache.shardingsphere.core.optimize.sharding.segment.insert;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.InsertColumns;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Insert columns for sharding.
 *
 * @author zhangliang
 * @author panjuan
 */
@ToString
public final class ShardingInsertColumns implements InsertColumns {
    
    private final String generateKeyColumnName;
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public ShardingInsertColumns(
            final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement, final Collection<String> derivedColumnNames) {
        generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName()).orNull();
        regularColumnNames = insertStatement.useDefaultColumns() 
                ? getRegularColumnNamesFromMetaData(shardingRule.getEncryptRule(), shardingTableMetaData, insertStatement, derivedColumnNames) : getRegularColumnNamesFromSQLStatement(insertStatement);
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(
            final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement, final Collection<String> derivedColumnNames) {
        Collection<String> allColumnNames = shardingTableMetaData.getAllColumnNames(insertStatement.getTable().getTableName());
        Collection<String> result = new LinkedHashSet<>(allColumnNames.size() - derivedColumnNames.size());
        String tableName = insertStatement.getTable().getTableName();
        for (String each : allColumnNames) {
            if (encryptRule.getCipherColumns(tableName).contains(each)) {
                result.add(encryptRule.getLogicColumn(tableName, each));
                continue;
            }
            if (!derivedColumnNames.contains(each)) {
                result.add(each);
            }
        }
        if (isGenerateKeyFromMetaData(allColumnNames, derivedColumnNames, insertStatement.getValueSize())) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private boolean isGenerateKeyFromMetaData(final Collection<String> allColumnNames, final Collection<String> derivedColumnNames, final int columnValueSize) {
        return null != generateKeyColumnName && allColumnNames.size() - derivedColumnNames.size() > columnValueSize;
    }
    
    private Collection<String> getRegularColumnNamesFromSQLStatement(final InsertStatement insertStatement) {
        Collection<String> result = new LinkedHashSet<>(insertStatement.getColumnNames());
        if (isGenerateKeyFromSQLStatement(insertStatement)) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private boolean isGenerateKeyFromSQLStatement(final InsertStatement insertStatement) {
        return null != generateKeyColumnName && !insertStatement.getColumns().isEmpty() && insertStatement.getColumns().size() > insertStatement.getValueSize();
    }
}
