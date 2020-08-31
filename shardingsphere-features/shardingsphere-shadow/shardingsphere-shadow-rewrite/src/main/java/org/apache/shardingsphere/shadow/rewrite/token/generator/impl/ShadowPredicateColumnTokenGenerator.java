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
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.shadow.rewrite.token.generator.BaseShadowSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
        Preconditions.checkState(((WhereAvailable) sqlStatementContext).getWhere().isPresent());
        Collection<SQLToken> result = new LinkedList<>();
        for (AndPredicate each : ((WhereAvailable) sqlStatementContext).getWhere().get().getAndPredicates()) {
            result.addAll(generateSQLTokens(((WhereAvailable) sqlStatementContext).getWhere().get(), each));
        }
        return result;
    }
    
    private Collection<SQLToken> generateSQLTokens(final WhereSegment whereSegment, final AndPredicate andPredicate) {
        Collection<SQLToken> result = new LinkedList<>();
        List<PredicateSegment> predicates = (LinkedList<PredicateSegment>) andPredicate.getPredicates();
        for (int i = 0; i < predicates.size(); i++) {
            if (!getShadowRule().getColumn().equals(predicates.get(i).getColumn().getIdentifier().getValue())) {
                continue;
            }
            if (1 == predicates.size()) {
                int startIndex = whereSegment.getStartIndex();
                int stopIndex = whereSegment.getStopIndex();
                result.add(new RemoveToken(startIndex, stopIndex));
                return result;
            }
            if (i == 0) {
                int startIndex = predicates.get(0).getStartIndex();
                int stopIndex = predicates.get(i + 1).getStartIndex() - 1;
                result.add(new RemoveToken(startIndex, stopIndex));
                return result;
            }
            int startIndex = predicates.get(i - 1).getStopIndex() + 1;
            int stopIndex = predicates.get(i).getStopIndex();
            result.add(new RemoveToken(startIndex, stopIndex));
            return result;
        }
        return result;
    }
}
