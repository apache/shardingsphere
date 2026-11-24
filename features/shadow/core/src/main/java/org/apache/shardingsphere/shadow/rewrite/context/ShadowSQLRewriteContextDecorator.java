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

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.rewrite.token.generator.ShadowInsertValuesTokenGenerator;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collections;

/**
 * SQL rewrite context decorator for shadow.
 */
@HighFrequencyInvocation
public final class ShadowSQLRewriteContextDecorator implements SQLRewriteContextDecorator<ShadowRule> {
    
    @Override
    public void decorate(final ShadowRule rule, final ConfigurationProperties props, final SQLRewriteContext sqlRewriteContext, final RouteContext routeContext) {
        if (routeContext.getRouteUnitInsertValueIndexes().isEmpty()) {
            return;
        }
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(new ShadowInsertValuesTokenGenerator(routeContext)));
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.REWRITE_ORDER;
    }
    
    @Override
    public Class<ShadowRule> getTypeClass() {
        return ShadowRule.class;
    }
}
