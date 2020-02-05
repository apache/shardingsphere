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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.shadow.rewrite.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.RemoveToken;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Remove shadow column token generator.
 *
 * @author zhyee
 */
public final class RemoveShadowColumnTokenGenerator extends BaseShadowSQLTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLTokenForShadow(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        return sqlStatementContext instanceof InsertSQLStatementContext && insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        Preconditions.checkState(sqlSegment.isPresent());
        Collection<RemoveToken> result = new LinkedList<>();
        LinkedList<ColumnSegment> columns = (LinkedList<ColumnSegment>) sqlSegment.get().getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (getShadowRule().getColumn().equals(columns.get(i).getName())) {
                if (i == 0) {
                    result.add(new RemoveToken(columns.get(i).getStartIndex(), columns.get(i + 1).getStartIndex() - 1));
                } else {
                    result.add(new RemoveToken(columns.get(i - 1).getStopIndex() + 1, columns.get(i).getStopIndex()));
                }
            }
        }
        return result;
    }
}
