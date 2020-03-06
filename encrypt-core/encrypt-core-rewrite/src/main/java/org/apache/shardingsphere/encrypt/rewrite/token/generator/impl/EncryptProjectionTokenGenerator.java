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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.strategy.EncryptTable;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Projection token generator for encrypt.
 */
@Setter
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator implements CollectionSQLTokenGenerator<SelectStatementContext>, QueryWithCipherColumnAware {
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getSqlStatement().getTables().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        ProjectionsSegment projectionsSegment = selectStatementContext.getSqlStatement().getProjections();
        // TODO process multiple tables
        String tableName = selectStatementContext.getSqlStatement().getTables().iterator().next().getTableName().getIdentifier().getValue();
        Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return Collections.emptyList();
        }
        for (ProjectionSegment each : projectionsSegment.getProjections()) {
            if (isEncryptLogicColumn(each, encryptTable.get())) {
                result.add(generateSQLToken((ColumnProjectionSegment) each, tableName));
            }
        }
        return result;
    }
    
    private boolean isEncryptLogicColumn(final ProjectionSegment projectionSegment, final EncryptTable encryptTable) {
        return projectionSegment instanceof ColumnProjectionSegment && encryptTable.getLogicColumns().contains(((ColumnProjectionSegment) projectionSegment).getColumn().getIdentifier().getValue());
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ColumnProjectionSegment segment, final String tableName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, segment.getColumn().getIdentifier().getValue());
        String columnName = plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : getEncryptRule().getCipherColumn(tableName, segment.getColumn().getIdentifier().getValue());
        return segment.getColumn().getOwner().isPresent() ? new SubstitutableColumnNameToken(segment.getColumn().getOwner().get().getStopIndex() + 2, segment.getStopIndex(), columnName)
                : new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), columnName);
    }
}
