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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rewrite.parameter.ShardingParameterRewriterBuilder;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;

/**
 * SQL rewrite context decorator for sharding.
 */
@Setter
public final class ShardingSQLRewriteContextDecorator implements SQLRewriteContextDecorator<ShardingRule> {
    
    @SuppressWarnings("unchecked")
    @Override
    public void decorate(final ShardingRule shardingRule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        for (ParameterRewriter each : new ShardingParameterRewriterBuilder(shardingRule, routeContext).getParameterRewriters(sqlRewriteContext.getSchemaMetaData())) {
            SQLStatementContext sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
            if (!sqlRewriteContext.getParameters().isEmpty() && each.isNeedRewrite(sqlStatementContext)) {
                each.rewrite(sqlRewriteContext.getParameterBuilder(), sqlStatementContext, sqlRewriteContext.getParameters());
            }
            if (sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                    && !sqlRewriteContext.getParameters().isEmpty() && each.isNeedRewrite(((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext())) {
                StandardParameterBuilder parameterBuilder = sqlRewriteContext.getParameterBuilder() instanceof GroupedParameterBuilder
                        ? ((GroupedParameterBuilder) sqlRewriteContext.getParameterBuilder()).getParameterBuilders().get(0) : (StandardParameterBuilder) sqlRewriteContext.getParameterBuilder();
                each.rewrite(parameterBuilder, ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext(), sqlRewriteContext.getParameters());
            }
        }
        sqlRewriteContext.addSQLTokenGenerators(new ShardingTokenGenerateBuilder(shardingRule, routeContext).getSQLTokenGenerators());
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
