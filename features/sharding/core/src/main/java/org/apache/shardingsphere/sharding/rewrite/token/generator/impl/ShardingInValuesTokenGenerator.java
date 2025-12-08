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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.Setter;
import org.apache.shardingsphere.infra.binder.context.segment.select.invalues.InValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInValuesToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingInValueItem;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Sharding IN values token generator.
 */
@Setter
public final class ShardingInValuesTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, RouteContextAware, IgnoreForSingleRoute {
    
    private RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isNeedInValuesRewrite();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        SelectStatementContext selectCtx = (SelectStatementContext) sqlStatementContext;
        InValueContext inValueContext = selectCtx.getInValueContext();
        if (null == inValueContext) {
            return Collections.emptyList();
        }
        ShardingInValuesToken token = createInValuesToken(inValueContext);
        return token.getInValueItems().isEmpty() ? Collections.emptyList() : Collections.singleton(token);
    }
    
    private ShardingInValuesToken createInValuesToken(final InValueContext inValueContext) {
        InExpression inExpression = inValueContext.getInExpression();
        ShardingInValuesToken result = new ShardingInValuesToken(
                inExpression.getRight().getStartIndex(),
                inExpression.getRight().getStopIndex());
        List<ExpressionSegment> valueExpressions = inValueContext.getValueExpressions();
        Iterator<Collection<DataNode>> dataNodesIterator = routeContext.getOriginalDataNodes().isEmpty()
                ? Collections.emptyIterator()
                : routeContext.getOriginalDataNodes().iterator();
        for (ExpressionSegment each : valueExpressions) {
            Collection<DataNode> dataNodes = dataNodesIterator.hasNext() ? dataNodesIterator.next() : Collections.emptyList();
            result.getInValueItems().add(new ShardingInValueItem(each, dataNodes));
        }
        return result;
    }
}
