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

package org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.rewriter.encrypt.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.sql.rewriter.encrypt.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.pojo.generic.SubstitutableColumnNameToken;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Projection token generator for encrypt.
 *
 * @author panjuan
 */
@Setter
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator, QueryWithCipherColumnAware {
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext.getSqlStatement() instanceof SelectStatement && !sqlStatementContext.getTablesContext().isEmpty())) {
            return false;
        }
        Optional<SelectItemsSegment> selectItemsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        return selectItemsSegment.isPresent() && !selectItemsSegment.get().getSelectItems().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        Optional<SelectItemsSegment> selectItemsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(SelectItemsSegment.class);
        Preconditions.checkState(selectItemsSegment.isPresent());
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(tableName);
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
    
    private SubstitutableColumnNameToken generateSQLToken(final ColumnSelectItemSegment segment, final String tableName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, segment.getName());
        String columnName = plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : getEncryptRule().getCipherColumn(tableName, segment.getName());
        return segment.getOwner().isPresent() ? new SubstitutableColumnNameToken(segment.getOwner().get().getStopIndex() + 2, segment.getStopIndex(), columnName)
                : new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), columnName);
    }
}
