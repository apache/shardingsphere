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

package org.apache.shardingsphere.infra.route.hook;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.hook.fixture.RoutingHookFixture;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class SPIRoutingHookTest {
    
    private final SPIRoutingHook spiRoutingHook = new SPIRoutingHook();
    
    @Mock
    private RouteContext routeContext;
    
    @Mock
    private PhysicalSchemaMetaData schemaMetaData;
    
    @Mock
    private Exception exception;
    
    @Test
    public void assertStart() {
        String sql = "SELECT * FROM table";
        spiRoutingHook.start(sql);
        RoutingHook routingHook = getFixtureHook();
        assertThat(routingHook, instanceOf(RoutingHookFixture.class));
        assertThat(((RoutingHookFixture) routingHook).getSql(), is(sql));
    }
    
    @Test
    public void assertFinishSuccess() {
        spiRoutingHook.finishSuccess(routeContext, schemaMetaData);
        RoutingHook routingHook = getFixtureHook();
        assertThat(routingHook, instanceOf(RoutingHookFixture.class));
        assertThat(((RoutingHookFixture) routingHook).getRouteContext(), is(routeContext));
        assertThat(((RoutingHookFixture) routingHook).getSchemaMetaData(), is(schemaMetaData));
    }
    
    @Test
    public void assertFinishFailure() {
        spiRoutingHook.finishFailure(exception);
        RoutingHook routingHook = getFixtureHook();
        assertThat(routingHook, instanceOf(RoutingHookFixture.class));
        assertThat(((RoutingHookFixture) routingHook).getCause(), is(exception));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private RoutingHook getFixtureHook() {
        Field routingHooksField = SPIRoutingHook.class.getDeclaredField("routingHooks");
        routingHooksField.setAccessible(true);
        return ((Collection<RoutingHook>) routingHooksField.get(spiRoutingHook)).iterator().next();
    }
}
