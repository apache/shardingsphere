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

package org.apache.shardingsphere.agent.core.yaml.swapper;

import org.apache.shardingsphere.agent.core.yaml.entity.Interceptor;
import org.apache.shardingsphere.agent.core.yaml.entity.Interceptors;
import org.apache.shardingsphere.agent.core.yaml.entity.TargetPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class InterceptorsYamlSwapperTest {
    
    @Test
    public void assertUnmarshal() {
        Interceptors actual = new InterceptorsYamlSwapper().unmarshal(getClass().getResourceAsStream("/interceptors.yaml"));
        assertThat(actual.getInterceptors().size(), is(5));
        List<Interceptor> actualInterceptors = new ArrayList<>(actual.getInterceptors());
        assertInterceptor(actualInterceptors.get(0), createExpectedInterceptor("org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask",
                "org.apache.shardingsphere.agent.metrics.api.advice.CommandExecutorTaskAdvice",
                null, null, Arrays.asList(createExpectedTargetPoint("run", "instance"), createExpectedTargetPoint("processException", "instance"))));
        assertInterceptor(actualInterceptors.get(1), createExpectedInterceptor("org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler",
                "org.apache.shardingsphere.agent.metrics.api.advice.ChannelHandlerAdvice",
                null, null, Arrays.asList(
                        createExpectedTargetPoint("channelActive", "instance"), createExpectedTargetPoint("channelRead", "instance"), createExpectedTargetPoint("channelInactive", "instance"))));
        assertInterceptor(actualInterceptors.get(2), createExpectedInterceptor("org.apache.shardingsphere.infra.route.engine.SQLRouteEngine",
                "org.apache.shardingsphere.agent.metrics.api.advice.SQLRouteEngineAdvice", null, null, Collections.singleton(createExpectedTargetPoint("route", "instance"))));
        assertInterceptor(actualInterceptors.get(3), createExpectedInterceptor("org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager",
                "org.apache.shardingsphere.agent.metrics.api.advice.TransactionAdvice",
                null, null, Arrays.asList(createExpectedTargetPoint("commit", "instance"), createExpectedTargetPoint("rollback", "instance"))));
        assertInterceptor(actualInterceptors.get(4), createExpectedInterceptor("org.apache.shardingsphere.infra.config.datasource.JDBCParameterDecoratorHelper",
                null, "org.apache.shardingsphere.agent.metrics.api.advice.DataSourceAdvice", null, Collections.singleton(createExpectedTargetPoint("decorate", "static"))));
    }
    
    private void assertInterceptor(final Interceptor actual, final Interceptor expected) {
        assertThat(actual.getTarget(), is(expected.getTarget()));
        assertThat(actual.getInstanceAdvice(), is(expected.getInstanceAdvice()));
        assertThat(actual.getStaticAdvice(), is(expected.getStaticAdvice()));
        assertThat(actual.getConstructAdvice(), is(expected.getConstructAdvice()));
        assertThat(actual.getPoints().isEmpty(), is(expected.getPoints().isEmpty()));
        Iterator<TargetPoint> expectedTargetPoints = expected.getPoints().iterator();
        for (TargetPoint each : actual.getPoints()) {
            TargetPoint expectedTargetPoint = expectedTargetPoints.next();
            assertThat(each.getName(), is(expectedTargetPoint.getName()));
            assertThat(each.getType(), is(expectedTargetPoint.getType()));
        }
    }
    
    private Interceptor createExpectedInterceptor(final String target, final String instanceAdvice, final String staticAdvice, final String constructAdvice, final Collection<TargetPoint> points) {
        Interceptor result = new Interceptor();
        result.setTarget(target);
        result.setInstanceAdvice(instanceAdvice);
        result.setStaticAdvice(staticAdvice);
        result.setConstructAdvice(constructAdvice);
        result.setPoints(points);
        return result;
    }
    
    private TargetPoint createExpectedTargetPoint(final String name, final String type) {
        TargetPoint result = new TargetPoint();
        result.setName(name);
        result.setType(type);
        return result;
    }
}
