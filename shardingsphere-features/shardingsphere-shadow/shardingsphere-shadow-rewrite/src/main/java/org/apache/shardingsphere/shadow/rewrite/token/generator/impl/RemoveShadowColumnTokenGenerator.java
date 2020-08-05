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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.shadow.rewrite.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;

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
        if (!(sqlStatementContext instanceof InsertStatementContext)) {
            return false;
        }
        Optional<InsertColumnsSegment> insertColumnsSegment = (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getInsertColumns();
        return insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = insertStatementContext.getSqlStatement().getInsertColumns();
        Preconditions.checkState(sqlSegment.isPresent());
        Collection<RemoveToken> result = new LinkedList<>();
        List<ColumnSegment> columns = (LinkedList<ColumnSegment>) sqlSegment.get().getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (getShadowRule().getColumn().equals(columns.get(i).getIdentifier().getValue())) {
                if (i == 0) {
                    result.add(new RemoveToken(columns.get(0).getStartIndex(), columns.get(i + 1).getStartIndex() - 1));
                } else {
                    result.add(new RemoveToken(columns.get(i - 1).getStopIndex() + 1, columns.get(i).getStopIndex()));
                }
            }
        }
        return result;
    }
}
