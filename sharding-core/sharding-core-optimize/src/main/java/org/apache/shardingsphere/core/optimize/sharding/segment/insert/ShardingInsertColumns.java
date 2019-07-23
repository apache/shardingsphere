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
public final class ShardingInsertColumns implements InsertColumns {
    
    private final InsertStatement insertStatement;
    
    private final String generateKeyColumnName;
    
    private final Collection<String> assistedQueryColumnNames;
    
    private final Collection<String> plainColumnNames;
    
    @Getter
    private final Collection<String> regularColumnNames;
    
    public ShardingInsertColumns(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final InsertStatement insertStatement) {
        this.insertStatement = insertStatement;
        generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTable().getTableName()).orNull();
        assistedQueryColumnNames = shardingRule.getEncryptRule().getEncryptEngine().getAssistedQueryColumns(insertStatement.getTable().getTableName());
        plainColumnNames = shardingRule.getEncryptRule().getEncryptEngine().getPlainColumns(insertStatement.getTable().getTableName());
        regularColumnNames = insertStatement.useDefaultColumns() ? getRegularColumnNamesFromMetaData(shardingTableMetaData, shardingRule.getEncryptRule()) : getRegularColumnNamesFromSQLStatement();
    }
    
    private Collection<String> getRegularColumnNamesFromMetaData(final ShardingTableMetaData shardingTableMetaData, final EncryptRule encryptRule) {
        Collection<String> allColumnNames = shardingTableMetaData.getAllColumnNames(insertStatement.getTable().getTableName());
        Collection<String> result = new LinkedHashSet<>(allColumnNames.size() - getAssistedQueryAndPlainColumnCount());
        for (String each : allColumnNames) {
            if (isNotAssistedQueryAndPlainColumns(each)) {
                result.add(each);
            }
            if (isCipherColumn(each, encryptRule)) {
                result.add(getLogicColumn(each, encryptRule));
            }
        }
        if (isGenerateKeyFromMetaData(allColumnNames)) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private boolean isNotAssistedQueryAndPlainColumns(final String each) {
        return !assistedQueryColumnNames.contains(each) && !plainColumnNames.contains(each);
    }
    
    private boolean isCipherColumn(final String each, final EncryptRule encryptRule) {
        return encryptRule.getEncryptEngine().getCipherColumns(insertStatement.getTable().getTableName()).contains(each);
    }
    
    private String getLogicColumn(final String each, final EncryptRule encryptRule) {
        return encryptRule.getEncryptEngine().getLogicColumn(insertStatement.getTable().getTableName(), each);
    }
    
    private boolean isGenerateKeyFromMetaData(final Collection<String> allColumnNames) {
        return null != generateKeyColumnName && allColumnNames.size() - getAssistedQueryAndPlainColumnCount() != insertStatement.getValueSize();
    }
    
    private Collection<String> getRegularColumnNamesFromSQLStatement() {
        Collection<String> result = new LinkedHashSet<>(insertStatement.getColumns().size(), 1);
        for (ColumnSegment each : insertStatement.getColumns()) {
            result.add(each.getName());
        }
        if (insertStatement.getSetAssignment().isPresent()) {
            for (AssignmentSegment each : insertStatement.getSetAssignment().get().getAssignments()) {
                result.add(each.getColumn().getName());
            }
        }
        if (isGenerateKeyFromSQLStatement()) {
            result.remove(generateKeyColumnName);
        }
        return result;
    }
    
    private boolean isGenerateKeyFromSQLStatement() {
        return null != generateKeyColumnName && insertStatement.getColumns().size() != insertStatement.getValueSize();
    }
    
    @Override
    public Collection<String> getAllColumnNames() {
        Collection<String> result = new LinkedHashSet<>(regularColumnNames.size() + getAssistedQueryAndPlainColumnCount() + 1);
        result.addAll(regularColumnNames);
        if (null != generateKeyColumnName && !regularColumnNames.contains(generateKeyColumnName)) {
            result.add(generateKeyColumnName);
        }
        result.addAll(assistedQueryColumnNames);
        result.addAll(plainColumnNames);
        return result;
    }
    
    private int getAssistedQueryAndPlainColumnCount() {
        return assistedQueryColumnNames.size() + plainColumnNames.size();
    }
}
