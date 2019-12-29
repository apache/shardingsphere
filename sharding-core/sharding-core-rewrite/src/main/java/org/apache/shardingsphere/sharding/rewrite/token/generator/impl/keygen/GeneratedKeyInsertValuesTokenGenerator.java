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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.InsertValueContext;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.relation.segment.insert.expression.DerivedSimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.generic.InsertValuesToken;

import java.util.Iterator;
import java.util.List;

/**
 * Insert values token generator for sharding.
 *
 * @author panjuan
 */
@Setter
public final class GeneratedKeyInsertValuesTokenGenerator extends BaseGeneratedKeyTokenGenerator implements OptionalSQLTokenGenerator, PreviousSQLTokensAware {
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    protected boolean isGenerateSQLToken(final InsertStatement insertStatement) {
        return !insertStatement.findSQLSegments(InsertValuesSegment.class).isEmpty();
    }
    
    @Override
    protected SQLToken generateSQLToken(final SQLStatementContext sqlStatementContext, final GeneratedKey generatedKey) {
        Optional<InsertValuesToken> result = findPreviousSQLToken();
        Preconditions.checkState(result.isPresent());
        Iterator<Comparable<?>> generatedValues = generatedKey.getGeneratedValues().descendingIterator();
        int count = 0;
        for (InsertValueContext each : ((InsertSQLStatementContext) sqlStatementContext).getInsertValueContexts()) {
            InsertValue insertValueToken = result.get().getInsertValues().get(count);
            DerivedSimpleExpressionSegment expressionSegment = isToAddDerivedLiteralExpression((InsertSQLStatementContext) sqlStatementContext, count)
                    ? new DerivedLiteralExpressionSegment(generatedValues.next()) : new DerivedParameterMarkerExpressionSegment(each.getParametersCount());
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
        return Optional.absent();
    }
    
    private boolean isToAddDerivedLiteralExpression(final InsertSQLStatementContext insertSQLStatementContext, final int insertValueCount) {
        return insertSQLStatementContext.getGroupedParameters().get(insertValueCount).isEmpty();
    }
}
