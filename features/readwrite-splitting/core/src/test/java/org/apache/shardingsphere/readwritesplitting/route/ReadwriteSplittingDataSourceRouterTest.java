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

package org.apache.shardingsphere.readwritesplitting.route;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.readwritesplitting.route.qualified.QualifiedReadwriteSplittingDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.route.standard.filter.ReadDataSourcesFilter;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
class ReadwriteSplittingDataSourceRouterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadwriteSplittingDataSourceGroupRule rule;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Test
    void assertRouteWithQualifiedRouters() {
        ReadwriteSplittingDataSourceRouter router = new ReadwriteSplittingDataSourceRouter(rule, mock(ConnectionContext.class));
        QualifiedReadwriteSplittingDataSourceRouter qualifiedRouter = mock(QualifiedReadwriteSplittingDataSourceRouter.class);
        when(qualifiedRouter.isQualified(sqlStatementContext, rule, hintValueContext)).thenReturn(true);
        when(qualifiedRouter.route(rule)).thenReturn("qualified_ds");
        setQualifiedRouters(router, qualifiedRouter);
        assertThat(router.route(sqlStatementContext, hintValueContext), is("qualified_ds"));
    }
    
    @Test
    void assertRouteWithStandardRouters() {
        when(rule.getLoadBalancer().getTargetName(any(), any())).thenReturn("standard_ds");
        ReadwriteSplittingDataSourceRouter router = new ReadwriteSplittingDataSourceRouter(rule, mock(ConnectionContext.class));
        setQualifiedRouters(router, mock(QualifiedReadwriteSplittingDataSourceRouter.class));
        when(ShardingSphereServiceLoader.getServiceInstances(ReadDataSourcesFilter.class)).thenReturn(Collections.emptyList());
        assertThat(router.route(sqlStatementContext, hintValueContext), is("standard_ds"));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setQualifiedRouters(final ReadwriteSplittingDataSourceRouter router, final QualifiedReadwriteSplittingDataSourceRouter qualifiedRouter) {
        Plugins.getMemberAccessor().set(ReadwriteSplittingDataSourceRouter.class.getDeclaredField("qualifiedRouters"), router, Collections.singleton(qualifiedRouter));
    }
}
