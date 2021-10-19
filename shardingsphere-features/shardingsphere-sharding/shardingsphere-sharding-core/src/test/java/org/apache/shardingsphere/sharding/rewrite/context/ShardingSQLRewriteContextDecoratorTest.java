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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLRewriteContextDecoratorTest {

    @Test
    public void assertDecorateForRouteContextWhenIsFederated() {
        ShardingSQLRewriteContextDecorator shardingSQLRewriteContextDecorator = new ShardingSQLRewriteContextDecorator();
        ShardingRule shardingRule = mock(ShardingRule.class);
        ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isFederated()).thenReturn(true);
        shardingSQLRewriteContextDecorator.decorate(shardingRule, configurationProperties, sqlRewriteContext, routeContext);
        Assert.assertNull(sqlRewriteContext.getSchema());
        Assert.assertNull(sqlRewriteContext.getSqlStatementContext());
        Assert.assertEquals(sqlRewriteContext.getParameters().size(), 0);
        Assert.assertNull(sqlRewriteContext.getParameterBuilder());
        Assert.assertEquals(sqlRewriteContext.getSqlTokens().size(), 0);
    }

    @Test
    public void assertDecorateForRouteContextWhenNotFederated() {
        List<Object> dummy = new ArrayList<>();
        dummy.add(new Object());
        ShardingSQLRewriteContextDecorator shardingSQLRewriteContextDecorator = new ShardingSQLRewriteContextDecorator();
        ShardingRule shardingRule = mock(ShardingRule.class);
        ConfigurationProperties configurationProperties = mock(ConfigurationProperties.class);
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        when(sqlRewriteContext.getParameters()).thenReturn(dummy);
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.isFederated()).thenReturn(false);
        shardingSQLRewriteContextDecorator.decorate(shardingRule, configurationProperties, sqlRewriteContext, routeContext);
        Assert.assertNotNull(sqlRewriteContext.getSqlTokens());
    }

    @Test
    public void assertGetOrder() {
        ShardingSQLRewriteContextDecorator shardingSQLRewriteContextDecorator = new ShardingSQLRewriteContextDecorator();
        int actual = shardingSQLRewriteContextDecorator.getOrder();
        Assert.assertEquals(actual, ShardingOrder.ORDER);
    }

    @Test
    public void assertGetTypeClass() {
        ShardingSQLRewriteContextDecorator shardingSQLRewriteContextDecorator = new ShardingSQLRewriteContextDecorator();
        Class<ShardingRule> actual = shardingSQLRewriteContextDecorator.getTypeClass();
        Assert.assertEquals(actual.getName(), ShardingRule.class.getName());
    }
}
