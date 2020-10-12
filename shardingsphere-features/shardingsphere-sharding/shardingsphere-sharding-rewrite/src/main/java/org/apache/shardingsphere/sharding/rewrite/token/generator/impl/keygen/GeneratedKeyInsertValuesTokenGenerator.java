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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Insert values token generator for sharding.
 */
@Setter
public final class GeneratedKeyInsertValuesTokenGenerator extends BaseGeneratedKeyTokenGenerator implements PreviousSQLTokensAware {
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatementContext insertStatementContext) {
        return !insertStatementContext.getSqlStatement().getValues().isEmpty();
    }
    
    @Override
    public SQLToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Optional<InsertValuesToken> result = findPreviousSQLToken();
        Preconditions.checkState(result.isPresent());
        Optional<GeneratedKeyContext> generatedKey = insertStatementContext.getGeneratedKeyContext();
        Preconditions.checkState(generatedKey.isPresent());
        Iterator<Comparable<?>> generatedValues = generatedKey.get().getGeneratedValues().iterator();
        int count = 0;
        for (InsertValueContext each : insertStatementContext.getInsertValueContexts()) {
            InsertValue insertValueToken = result.get().getInsertValues().get(count);
            DerivedSimpleExpressionSegment expressionSegment = isToAddDerivedLiteralExpression(insertStatementContext, count)
                    ? new DerivedLiteralExpressionSegment(generatedValues.next()) : new DerivedParameterMarkerExpressionSegment(each.getParameterCount());
            insertValueToken.getValues().add(expressionSegment);
            count++;
        }
        return result.get();
    }
    
    private Optional<InsertValuesToken> findPreviousSQLToken() {
        for (SQLToken each : previousSQLTokens) {
            if (each instanceof InsertValuesToken) {
                return Optional.of((InsertValuesToken) each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isToAddDerivedLiteralExpression(final InsertStatementContext insertStatementContext, final int insertValueCount) {
        return insertStatementContext.getGroupedParameters().get(insertValueCount).isEmpty();
    }
}
