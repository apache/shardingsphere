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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewritersBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.builder.SQLTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rewrite.parameter.ShardingParameterRewritersRegistry;
import org.apache.shardingsphere.sharding.rewrite.token.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;

import java.util.Collection;

/**
 * SQL rewrite context decorator for sharding.
 */
@HighFrequencyInvocation
public final class ShardingSQLRewriteContextDecorator implements SQLRewriteContextDecorator<ShardingRule> {
    
    @Override
    public void decorate(final ShardingRule rule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        SQLStatementContext sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        if (!isAlterOrDropIndexStatement(sqlStatementContext) && !isCursorContextAvailableStatement(sqlStatementContext) && !containsShardingTable(rule, sqlStatementContext)) {
            return;
        }
        if (!sqlRewriteContext.getParameters().isEmpty()) {
            Collection<ParameterRewriter> parameterRewriters = new ParameterRewritersBuilder(sqlStatementContext).build(new ShardingParameterRewritersRegistry(routeContext));
            rewriteParameters(sqlRewriteContext, parameterRewriters);
        }
        SQLTokenGeneratorBuilder sqlTokenGeneratorBuilder = new ShardingTokenGenerateBuilder(rule, routeContext, sqlStatementContext);
        sqlRewriteContext.addSQLTokenGenerators(sqlTokenGeneratorBuilder.getSQLTokenGenerators());
    }
    
    private boolean isAlterOrDropIndexStatement(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement() instanceof AlterIndexStatement || sqlStatementContext.getSqlStatement() instanceof DropIndexStatement;
    }
    
    private boolean isCursorContextAvailableStatement(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent();
    }
    
    private boolean containsShardingTable(final ShardingRule rule, final SQLStatementContext sqlStatementContext) {
        for (SimpleTableSegment each : sqlStatementContext.getTablesContext().getSimpleTables()) {
            if (rule.isShardingTable(each.getTableName().getIdentifier().getValue())) {
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
