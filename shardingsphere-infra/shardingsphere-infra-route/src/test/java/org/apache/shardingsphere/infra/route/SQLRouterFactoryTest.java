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

package org.apache.shardingsphere.infra.route;

import org.apache.shardingsphere.infra.route.fixture.router.SQLRouterFixture;
import org.apache.shardingsphere.infra.route.fixture.rule.RouteRuleFixture;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLRouterFactoryTest {
    
    @Test
    public void assertGetInstances() {
        RouteRuleFixture rule = mock(RouteRuleFixture.class);
        Map<ShardingSphereRule, SQLRouter> actual = SQLRouterFactory.getInstances(Collections.singleton(rule));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(rule), instanceOf(SQLRouterFixture.class));
    }
}
