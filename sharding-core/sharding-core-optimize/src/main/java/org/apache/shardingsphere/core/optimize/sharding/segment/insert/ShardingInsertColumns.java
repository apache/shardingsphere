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
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.InsertColumns;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Insert columns for sharding.
 *
 * @author zhangliang
 */
public final class ShardingInsertColumns implements InsertColumns {
    
    private final String generateKeyColumnName;
    
    private final Collection<String> assistedQueryColumnNames;
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public ShardingInsertColumns(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName()).orNull();
        assistedQueryColumnNames = shardingRule.getEncryptRule().getEncryptEngine().getAssistedQueryColumns(insertStatement.getTable().getTableName());
        regularColumnNames = insertStatement.useDefaultColumns() ? getRegularColumnNamesFromMetaData(shardingTableMetaData, insertStatement) : getRegularColumnNamesFromSQLStatement(insertStatement);
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        Collection<String> allColumnNames = shardingTableMetaData.getAllColumnNames(insertStatement.getTable().getTableName());
        Collection<String> result = new LinkedHashSet<>(allColumnNames.size() - assistedQueryColumnNames.size());
        for (String each : allColumnNames) {
            if (!assistedQueryColumnNames.contains(each)) {
                result.add(each);
            }
        }
        if (isGenerateKeyFromMetaData(insertStatement, allColumnNames)) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private boolean isGenerateKeyFromMetaData(final InsertStatement insertStatement, final Collection<String> allColumnNames) {
        return null != generateKeyColumnName && allColumnNames.size() - assistedQueryColumnNames.size() != insertStatement.getValueSize();
    }
    
    private Collection<String> getRegularColumnNamesFromSQLStatement(final InsertStatement insertStatement) {
        Collection<String> result = new LinkedHashSet<>(insertStatement.getColumns().size(), 1);
        for (ColumnSegment each : insertStatement.getColumns()) {
            result.add(each.getName());
        }
        if (insertStatement.getSetAssignment().isPresent()) {
            for (AssignmentSegment each : insertStatement.getSetAssignment().get().getAssignments()) {
                result.add(each.getColumn().getName());
            }
        }
        if (isGenerateKeyFromSQLStatement(insertStatement)) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private boolean isGenerateKeyFromSQLStatement(final InsertStatement insertStatement) {
        return null != generateKeyColumnName && insertStatement.getColumns().size() != insertStatement.getValueSize();
    }
    
    @Override
    public Collection<String> getAllColumnNames() {
        Collection<String> result = new LinkedHashSet<>(regularColumnNames.size() + assistedQueryColumnNames.size() + 1);
        result.addAll(regularColumnNames);
        if (null != generateKeyColumnName && !regularColumnNames.contains(generateKeyColumnName)) {
            result.add(generateKeyColumnName);
        }
        result.addAll(assistedQueryColumnNames);
        return result;
    }
}
