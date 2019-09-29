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

package org.apache.shardingsphere.core.rewrite.token.generator.optional.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.generator.ShardingConditionsAware;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertValuesToken;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.rule.DataNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Insert values token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertValuesTokenGenerator implements OptionalSQLTokenGenerator, ShardingConditionsAware {
    
    private ShardingConditions shardingConditions;
    
    @Override
    public Optional<InsertValuesToken> generateSQLToken(final RewriteStatement rewriteStatement, final ParameterBuilder parameterBuilder) {
        Collection<InsertValuesSegment> insertValuesSegments = rewriteStatement.getSqlStatementContext().getSqlStatement().findSQLSegments(InsertValuesSegment.class);
        return isNeedToGenerateSQLToken(rewriteStatement.getSqlStatementContext(), insertValuesSegments)
                ? Optional.of(createInsertValuesToken(rewriteStatement, insertValuesSegments)) : Optional.<InsertValuesToken>absent();
    }
    
    private boolean isNeedToGenerateSQLToken(final SQLStatementContext sqlStatementContext, final Collection<InsertValuesSegment> insertValuesSegments) {
        return sqlStatementContext.getSqlStatement() instanceof InsertStatement && !insertValuesSegments.isEmpty();
    }
    
    private InsertValuesToken createInsertValuesToken(final RewriteStatement rewriteStatement, final Collection<InsertValuesSegment> insertValuesSegments) {
        InsertValuesToken result = new InsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        Iterator<ShardingCondition> shardingConditionIterator = null;
        if (rewriteStatement.getSqlStatementContext() instanceof InsertSQLStatementContext) {
            shardingConditionIterator = null == shardingConditions || shardingConditions.getConditions().isEmpty() ? null : shardingConditions.getConditions().iterator();
        }
        for (InsertValueContext each : ((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext()).getInsertValueContexts()) {
            Collection<DataNode> dataNodes = null == shardingConditionIterator ? Collections.<DataNode>emptyList() : shardingConditionIterator.next().getDataNodes();
            result.addInsertValueToken(each.getValueExpressions(), dataNodes);
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> insertValuesSegments) {
        int result = insertValuesSegments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : insertValuesSegments) {
            result = result > each.getStartIndex() ? each.getStartIndex() : result;
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> insertValuesSegments) {
        int result = insertValuesSegments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : insertValuesSegments) {
            result = result < each.getStopIndex() ? each.getStopIndex() : result;
        }
        return result;
    }
}
