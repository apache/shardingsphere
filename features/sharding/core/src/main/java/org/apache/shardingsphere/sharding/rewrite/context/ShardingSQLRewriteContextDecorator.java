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

package org.apache.shardingsphere.sharding.rewrite.context;

import lombok.Setter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.CursorAvailable;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rewrite.parameter.ShardingParameterRewriterBuilder;
import org.apache.shardingsphere.sharding.rewrite.token.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Collection;

/**
 * SQL rewrite context decorator for sharding.
 */
@HighFrequencyInvocation
@Setter
public final class ShardingSQLRewriteContextDecorator implements SQLRewriteContextDecorator<ShardingRule> {
    
    @Override
    public void decorate(final ShardingRule shardingRule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        SQLStatementContext sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        if (!isAlterOrDropIndexStatement(sqlStatementContext) && !isCursorAvailableStatement(sqlStatementContext) && !containsShardingTable(shardingRule, sqlStatementContext)) {
            return;
        }
        if (!sqlRewriteContext.getParameters().isEmpty()) {
            Collection<ParameterRewriter> parameterRewriters =
                    new ShardingParameterRewriterBuilder(shardingRule, routeContext, sqlRewriteContext.getDatabase().getSchemas(), sqlStatementContext).getParameterRewriters();
            rewriteParameters(sqlRewriteContext, parameterRewriters);
        }
        sqlRewriteContext.addSQLTokenGenerators(new ShardingTokenGenerateBuilder(shardingRule, routeContext, sqlStatementContext).getSQLTokenGenerators());
    }
    
    private boolean isAlterOrDropIndexStatement(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof AlterIndexStatementContext || sqlStatementContext instanceof DropIndexStatementContext;
    }
    
    private boolean isCursorAvailableStatement(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof CursorAvailable;
    }
    
    private boolean containsShardingTable(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof TableAvailable)) {
            return false;
        }
        for (SimpleTableSegment each : ((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables()) {
            if (shardingRule.isShardingTable(each.getTableName().getIdentifier().getValue())) {
                return true;
            }
        }
        return false;
    }
    
    private void rewriteParameters(final SQLRewriteContext sqlRewriteContext, final Collection<ParameterRewriter> parameterRewriters) {
        for (ParameterRewriter each : parameterRewriters) {
            each.rewrite(sqlRewriteContext.getParameterBuilder(), sqlRewriteContext.getSqlStatementContext(), sqlRewriteContext.getParameters());
        }
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
