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

package org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.impl;

import lombok.Setter;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.SQLRouteResultAware;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.sql.token.pojo.impl.InsertValuesToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
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
public final class InsertValuesTokenGenerator implements OptionalSQLTokenGenerator, SQLRouteResultAware {
    
    private SQLRouteResult sqlRouteResult;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof InsertStatement && !sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class).isEmpty();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatementContext.getSqlStatement().findSQLSegments(InsertValuesSegment.class);
        InsertValuesToken result = new InsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments));
        Iterator<ShardingCondition> shardingConditionIterator = null == sqlRouteResult || sqlRouteResult.getShardingConditions().getConditions().isEmpty()
                ? null : sqlRouteResult.getShardingConditions().getConditions().iterator();
        for (InsertValueContext each : ((InsertSQLStatementContext) sqlStatementContext).getInsertValueContexts()) {
            Collection<DataNode> dataNodes = null == shardingConditionIterator ? Collections.<DataNode>emptyList() : shardingConditionIterator.next().getDataNodes();
            result.addInsertValueToken(each.getValueExpressions(), dataNodes);
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = result > each.getStartIndex() ? each.getStartIndex() : result;
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = result < each.getStopIndex() ? each.getStopIndex() : result;
        }
        return result;
    }
}
