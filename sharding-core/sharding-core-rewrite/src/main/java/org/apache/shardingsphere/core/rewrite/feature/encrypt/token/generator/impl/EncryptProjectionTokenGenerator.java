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

package org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.EncryptRuleAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.generator.QueryWithCipherColumnAware;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.pojo.EncryptColumnNameToken;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Encrypt projection token generator.
 *
 * @author panjuan
 */
@Setter
public final class EncryptProjectionTokenGenerator implements CollectionSQLTokenGenerator, EncryptRuleAware, QueryWithCipherColumnAware {
    
    private EncryptRule encryptRule;
    
    private boolean queryWithCipherColumn;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext.getSqlStatement() instanceof SelectStatement && !sqlStatementContext.getTablesContext().isEmpty())) {
            return false;
        }
        Optional<SelectItemsSegment> selectItemsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        return selectItemsSegment.isPresent() && !selectItemsSegment.get().getSelectItems().isEmpty();
    }
    
    @Override
    public Collection<EncryptColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<EncryptColumnNameToken> result = new LinkedList<>();
        Optional<SelectItemsSegment> selectItemsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        Preconditions.checkState(selectItemsSegment.isPresent());
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        for (SelectItemSegment each : selectItemsSegment.get().getSelectItems()) {
            if (isEncryptLogicColumn(each, encryptTable.get())) {
                result.add(generateSQLToken((ColumnSelectItemSegment) each, tableName));
            }
        }
        return result;
    }
    
    private boolean isEncryptLogicColumn(final SelectItemSegment selectItemSegment, final EncryptTable encryptTable) {
        return selectItemSegment instanceof ColumnSelectItemSegment && encryptTable.getLogicColumns().contains(((ColumnSelectItemSegment) selectItemSegment).getName());
    }
    
    private EncryptColumnNameToken generateSQLToken(final ColumnSelectItemSegment segment, final String tableName) {
        Optional<String> plainColumn = encryptRule.findPlainColumn(tableName, segment.getName());
        String columnName = plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : encryptRule.getCipherColumn(tableName, segment.getName());
        return segment.getOwner().isPresent() ? new EncryptColumnNameToken(segment.getOwner().get().getStopIndex() + 2, segment.getStopIndex(), columnName)
                : new EncryptColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), columnName);
    }
}
