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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectEncryptItemToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Select cipher item token generator.
 *
 * @author panjuan
 */
public final class SelectEncryptItemTokenGenerator implements CollectionSQLTokenGenerator<EncryptRule> {
    
    @Override
    public Collection<SelectEncryptItemToken> generateSQLTokens(final OptimizedStatement optimizedStatement,
                                                                final ParameterBuilder parameterBuilder, final EncryptRule rule, final boolean isQueryWithCipherColumn) {
        if (!isNeedToGenerateSQLToken(optimizedStatement)) {
            return Collections.emptyList();
        }
        return createSelectCipherItemTokens(rule, optimizedStatement, isQueryWithCipherColumn);
    }
    
    private boolean isNeedToGenerateSQLToken(final OptimizedStatement optimizedStatement) {
        if (!isSelectStatementWithTable(optimizedStatement)) {
            return false;
        }
        Optional<SelectItemsSegment> selectItemsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SelectItemsSegment.class);
        return selectItemsSegment.isPresent() && !selectItemsSegment.get().getSelectItems().isEmpty();
    }
    
    private boolean isSelectStatementWithTable(final OptimizedStatement optimizedStatement) {
        return optimizedStatement.getSQLStatement() instanceof SelectStatement && !optimizedStatement.getTables().isEmpty();
    }
    
    private Collection<SelectEncryptItemToken> createSelectCipherItemTokens(final EncryptRule encryptRule, final OptimizedStatement optimizedStatement, final boolean isQueryWithCipherColumn) {
        Collection<SelectEncryptItemToken> result = new LinkedList<>();
        Optional<SelectItemsSegment> selectItemsSegment = optimizedStatement.getSQLStatement().findSQLSegment(SelectItemsSegment.class);
        if (!selectItemsSegment.isPresent()) {
            return Collections.emptyList();
        }
        String tableName = optimizedStatement.getTables().getSingleTableName();
        Collection<String> logicColumns = encryptRule.getLogicColumns(tableName);
        for (SelectItemSegment each : selectItemsSegment.get().getSelectItems()) {
            if (isLogicColumn(each, logicColumns)) {
                result.add(createSelectCipherItemToken(each, tableName, encryptRule, isQueryWithCipherColumn));
            }
        }
        return result;
    }
    
    private boolean isLogicColumn(final SelectItemSegment each, final Collection<String> logicColumns) {
        return each instanceof ColumnSelectItemSegment && logicColumns.contains(((ColumnSelectItemSegment) each).getName());
    }
    
    private SelectEncryptItemToken createSelectCipherItemToken(final SelectItemSegment selectItemSegment, 
                                                               final String tableName, final EncryptRule encryptRule, final boolean isQueryWithCipherColumn) {
        String columnName = ((ColumnSelectItemSegment) selectItemSegment).getName();
        Optional<String> plainColumn = encryptRule.getPlainColumn(tableName, columnName);
        if (!isQueryWithCipherColumn && plainColumn.isPresent()) {
            return createSelectEncryptItemToken(selectItemSegment, plainColumn.get());
        }
        return createSelectEncryptItemToken(selectItemSegment, encryptRule.getCipherColumn(tableName, columnName));
    }
    
    private SelectEncryptItemToken createSelectEncryptItemToken(final SelectItemSegment selectItemSegment, final String columnName) {
        Optional<TableSegment> owner = ((ColumnSelectItemSegment) selectItemSegment).getOwner();
        if (owner.isPresent()) {
            return new SelectEncryptItemToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), columnName, owner.get().getTableName());
        }
        return new SelectEncryptItemToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), columnName);
    }
}
