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
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.shadow.rewrite.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionExtractUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Predicate column token generator for shadow.
 */
@Setter
public final class ShadowPredicateColumnTokenGenerator extends BaseShadowSQLTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    protected boolean isGenerateSQLTokenForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof WhereAvailable && ((WhereAvailable) sqlStatementContext).getWhere().isPresent();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Optional<WhereSegment> whereOptional = ((WhereAvailable) sqlStatementContext).getWhere();
        Preconditions.checkState(whereOptional.isPresent());
        WhereSegment whereSegment = whereOptional.get();
        ExpressionSegment expression = whereSegment.getExpr();
        Collection<SQLToken> result = new LinkedList<>();
        for (AndPredicate each : ExpressionExtractUtil.getAndPredicates(expression)) {
            result.addAll(generateSQLTokens(whereSegment, each));
        }
        return result;
    }
    
    private Collection<SQLToken> generateSQLTokens(final WhereSegment whereSegment, final AndPredicate andPredicate) {
        Collection<SQLToken> result = new LinkedList<>();
        Collection<ExpressionSegment> predicates = andPredicate.getPredicates();
        int index = 0;
        int previousElementStopIndex = 0;
        Iterator<ExpressionSegment> iterator = predicates.iterator();
        while (iterator.hasNext()) {
            ExpressionSegment each = iterator.next();
            for (ColumnSegment column : ColumnExtractor.extract(each)) {
                if (!getShadowRule().getColumn().equals(column.getIdentifier().getValue())) {
                    continue;
                }
                if (1 == predicates.size()) {
                    result.add(new RemoveToken(whereSegment.getStartIndex(), whereSegment.getStopIndex()));
                    return result;
                }
                result.add(isFirstElement(index) ? new RemoveToken(each.getStartIndex(), iterator.next().getStartIndex() - 1)
                        : new RemoveToken(previousElementStopIndex + 1, each.getStopIndex()));
                return result;
            }
            previousElementStopIndex = each.getStopIndex();
            index++;
        }
        return result;
    }
    
    private boolean isFirstElement(final int index) {
        return 0 == index;
    }
}
