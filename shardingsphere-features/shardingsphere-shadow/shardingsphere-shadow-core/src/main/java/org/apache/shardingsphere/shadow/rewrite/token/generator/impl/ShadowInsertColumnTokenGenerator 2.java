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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Remove shadow column token generator.
 */
public final class ShadowInsertColumnTokenGenerator extends BaseShadowSQLTokenGenerator implements CollectionSQLTokenGenerator<InsertStatementContext> {
    
    @Override
    protected boolean isGenerateSQLTokenForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && isContainShadowColumn(((InsertStatementContext) sqlStatementContext).getSqlStatement());
    }
    
    private boolean isContainShadowColumn(final InsertStatement sqlStatement) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatement.getInsertColumns();
        return insertColumnsSegment.isPresent() && !insertColumnsSegment.get().getColumns().isEmpty();
    }
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(final InsertStatementContext insertStatementContext) {
        Optional<InsertColumnsSegment> sqlSegment = insertStatementContext.getSqlStatement().getInsertColumns();
        Collection<RemoveToken> result = new LinkedList<>();
        sqlSegment.ifPresent(insertColumnsSegment -> generateRemoveTokenForShadow(insertColumnsSegment, result));
        return result;
    }
    
    private void generateRemoveTokenForShadow(final InsertColumnsSegment insertColumnsSegment, final Collection<RemoveToken> removeTokens) {
        Collection<ColumnSegment> columnSegments = insertColumnsSegment.getColumns();
        String shadowColumn = getShadowColumn();
        int index = 0;
        int previousElementStopIndex = 0;
        Iterator<ColumnSegment> iterator = columnSegments.iterator();
        while (iterator.hasNext()) {
            ColumnSegment each = iterator.next();
            if (shadowColumn.equals(each.getIdentifier().getValue())) {
                removeTokens.add(isFirstElement(index) ? new RemoveToken(each.getStartIndex(), iterator.next().getStartIndex() - 1)
                        : new RemoveToken(previousElementStopIndex + 1, each.getStopIndex()));
            }
            previousElementStopIndex = each.getStopIndex();
            index++;
        }
    }
    
    private boolean isFirstElement(final int index) {
        return 0 == index;
    }
}
