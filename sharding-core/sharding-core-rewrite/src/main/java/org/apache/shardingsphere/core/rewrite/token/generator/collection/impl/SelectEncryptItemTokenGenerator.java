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

package org.apache.shardingsphere.core.rewrite.token.generator.collection.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.token.generator.collection.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.SelectEncryptItemToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Select cipher item token generator.
 *
 * @author panjuan
 */
@Setter
public final class SelectEncryptItemTokenGenerator implements CollectionSQLTokenGenerator, EncryptRuleAware, QueryWithCipherColumnAware {
    
    private EncryptRule encryptRule;
    
    private boolean queryWithCipherColumn;
    
    @Override
    public Collection<SelectEncryptItemToken> generateSQLTokens(final RewriteStatement rewriteStatement, final ParameterBuilder parameterBuilder) {
        if (!isNeedToGenerateSQLToken(rewriteStatement.getSqlStatementContext())) {
            return Collections.emptyList();
        }
        return createSelectCipherItemTokens(rewriteStatement.getSqlStatementContext());
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!isSelectStatementWithTable(sqlStatementContext)) {
            return false;
        }
        Optional<SelectItemsSegment> selectItemsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        return selectItemsSegment.isPresent() && !selectItemsSegment.get().getSelectItems().isEmpty();
    }
    
    private boolean isSelectStatementWithTable(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof SelectStatement && !sqlStatementContext.getTablesContext().isEmpty();
    }
    
    private Collection<SelectEncryptItemToken> createSelectCipherItemTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SelectEncryptItemToken> result = new LinkedList<>();
        Optional<SelectItemsSegment> selectItemsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        if (!selectItemsSegment.isPresent()) {
            return Collections.emptyList();
        }
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        for (SelectItemSegment each : selectItemsSegment.get().getSelectItems()) {
            if (isLogicColumn(each, encryptTable.get())) {
                result.add(createSelectCipherItemToken((ColumnSelectItemSegment) each, tableName));
            }
        }
        return result;
    }
    
    private boolean isLogicColumn(final SelectItemSegment selectItemSegment, final EncryptTable encryptTable) {
        return selectItemSegment instanceof ColumnSelectItemSegment && encryptTable.getLogicColumns().contains(((ColumnSelectItemSegment) selectItemSegment).getName());
    }
    
    private SelectEncryptItemToken createSelectCipherItemToken(final ColumnSelectItemSegment columnSelectItemSegment, final String tableName) {
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, columnSelectItemSegment.getName());
        String columnName = plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : encryptRule.getCipherColumn(tableName, columnSelectItemSegment.getName());
        return createSelectEncryptItemToken(columnSelectItemSegment, columnName);
    }
    
    private SelectEncryptItemToken createSelectEncryptItemToken(final SelectItemSegment selectItemSegment, final String columnName) {
        Optional<TableSegment> owner = ((ColumnSelectItemSegment) selectItemSegment).getOwner();
        if (owner.isPresent()) {
            return new SelectEncryptItemToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), columnName, owner.get().getTableName());
        }
        return new SelectEncryptItemToken(selectItemSegment.getStartIndex(), selectItemSegment.getStopIndex(), columnName);
    }
}
