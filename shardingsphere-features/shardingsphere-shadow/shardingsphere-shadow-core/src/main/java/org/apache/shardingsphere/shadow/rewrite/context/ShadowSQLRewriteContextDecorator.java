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

package org.apache.shardingsphere.shadow.rewrite.context;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.rewrite.parameter.ShadowParameterRewriterBuilder;
import org.apache.shardingsphere.shadow.rewrite.token.ShadowTokenGenerateBuilder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.List;

/**
 * SQL rewrite context decorator for shadow.
 */
public final class ShadowSQLRewriteContextDecorator implements SQLRewriteContextDecorator<ShadowRule> {
    
    @Override
    public void decorate(final ShadowRule shadowRule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        doShadowDecorate(shadowRule, sqlRewriteContext);
    }
    
    private void doShadowDecorate(final ShadowRule shadowRule, final SQLRewriteContext sqlRewriteContext) {
        doParameterRewriterDecorate(shadowRule, sqlRewriteContext);
        sqlRewriteContext.addSQLTokenGenerators(new ShadowTokenGenerateBuilder(shadowRule).getSQLTokenGenerators());
    }
    
    @SuppressWarnings("unchecked")
    private void doParameterRewriterDecorate(final ShadowRule shadowRule, final SQLRewriteContext sqlRewriteContext) {
        List<Object> parameters = sqlRewriteContext.getParameters();
        SQLStatementContext<?> sqlStatementContext = sqlRewriteContext.getSqlStatementContext();
        for (ParameterRewriter each : new ShadowParameterRewriterBuilder(shadowRule).getParameterRewriters(sqlRewriteContext.getSchema())) {
            if (!parameters.isEmpty() && each.isNeedRewrite(sqlStatementContext)) {
                ParameterBuilder parameterBuilder = sqlRewriteContext.getParameterBuilder();
                each.rewrite(parameterBuilder, sqlStatementContext, parameters);
            }
        }
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
    
    @Override
    public Class<ShadowRule> getTypeClass() {
        return ShadowRule.class;
    }
}
