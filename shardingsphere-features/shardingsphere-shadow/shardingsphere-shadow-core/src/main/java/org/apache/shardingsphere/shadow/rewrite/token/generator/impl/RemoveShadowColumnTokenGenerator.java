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

package org.apache.shardingsphere.shadow.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.shadow.rewrite.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Remove shadow column token generator.
 */
public final class RemoveShadowColumnTokenGenerator extends BaseShadowSQLTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForShadow(final SQLStatementContext sqlStatementContext) {
        InsertStatementContext insertStatementContext;
        if (sqlStatementContext instanceof InsertStatementContext) {
            insertStatementContext = (InsertStatementContext) sqlStatementContext;
            Optional<InsertColumnsSegment> insertColumnsSegment = insertStatementContext.getSqlStatement().getInsertColumns();
            return insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
        }
        return false;
    }
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = insertStatementContext.getSqlStatement().getInsertColumns();
        Collection<RemoveToken> result = new LinkedList<>();
        sqlSegment.ifPresent(insertColumnsSegment -> generateRemoveTokenForShadow(insertColumnsSegment, result));
        return result;
    }
    
    private void generateRemoveTokenForShadow(final InsertColumnsSegment insertColumnsSegment, final Collection<RemoveToken> removeTokens) {
        List<ColumnSegment> columnSegments = (LinkedList<ColumnSegment>) insertColumnsSegment.getColumns();
        String shadowColumn = getShadowColumn();
        for (int i = 0; i < columnSegments.size(); i++) {
            ColumnSegment columnSegment = columnSegments.get(i);
            if (shadowColumn.equals(columnSegment.getIdentifier().getValue())) {
                RemoveToken removeToken = i == 0 ? new RemoveToken(columnSegments.get(i).getStartIndex(), columnSegments.get(i + 1).getStartIndex() - 1)
                        : new RemoveToken(columnSegments.get(i - 1).getStopIndex() + 1, columnSegments.get(i).getStopIndex());
                removeTokens.add(removeToken);
            }
        }
    }
}
