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

package org.apache.shardingsphere.core.optimize.encrypt.segment;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.InsertColumns;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;

/**
 * Insert columns for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
@ToString
public final class EncryptInsertColumns implements InsertColumns {
    
    private final Collection<String> assistedQueryAndPlainColumnNames;
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public EncryptInsertColumns(final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        assistedQueryAndPlainColumnNames = encryptRule.getAssistedQueryAndPlainColumns(insertStatement.getTable().getTableName());
        regularColumnNames = insertStatement.useDefaultColumns() 
                ? getRegularColumnNamesFromMetaData(encryptRule, shardingTableMetaData, insertStatement) : getColumnNamesFromSQLStatement(insertStatement);
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        Collection<String> allColumnNames = shardingTableMetaData.getAllColumnNames(insertStatement.getTable().getTableName());
        Collection<String> result = new LinkedHashSet<>(allColumnNames.size() - assistedQueryAndPlainColumnNames.size());
        String tableName = insertStatement.getTable().getTableName();
        for (String each : allColumnNames) {
            if (isCipherColumn(encryptRule, tableName, each)) {
                result.add(getLogicColumn(encryptRule, tableName, each));
                continue;
            }
            if (!isAssistedQueryAndPlainColumns(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isAssistedQueryAndPlainColumns(final String columnName) {
        return assistedQueryAndPlainColumnNames.contains(columnName);
    }
    
    private boolean isCipherColumn(final EncryptRule encryptRule, final String tableName, final String columnName) {
        return encryptRule.getCipherColumns(tableName).contains(columnName);
    }
    
    private String getLogicColumn(final EncryptRule encryptRule, final String tableName, final String columnName) {
        return encryptRule.getLogicColumn(tableName, columnName);
    }
    
    private Collection<String> getColumnNamesFromSQLStatement(final InsertStatement insertStatement) {
        Collection<String> result = new LinkedList<>();
        for (ColumnSegment each : insertStatement.getColumns()) {
            result.add(each.getName());
        }
        if (insertStatement.getSetAssignment().isPresent()) {
            for (AssignmentSegment each : insertStatement.getSetAssignment().get().getAssignments()) {
                result.add(each.getColumn().getName());
            }
        }
        return result;
    }
    
    @Override
    public Collection<String> getAllColumnNames() {
        Collection<String> result = new LinkedHashSet<>(regularColumnNames.size() + assistedQueryAndPlainColumnNames.size());
        result.addAll(regularColumnNames);
        result.addAll(assistedQueryAndPlainColumnNames);
        return result;
    }
}
