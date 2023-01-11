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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.proxy;

import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.core.advice.MetricsAdviceBaseTest;
import org.apache.shardingsphere.agent.plugin.metrics.core.advice.MockTargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.metrics.core.constant.MetricIds;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.FixtureWrapper;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class RollbackTransactionsCountAdviceTest extends MetricsAdviceBaseTest {
    
    private final RollbackTransactionsCountAdvice advice = new RollbackTransactionsCountAdvice();
    
    @Test
    public void assertMethod() {
        advice.beforeMethod(new MockTargetAdviceObject(), mock(Method.class), new Object[]{});
        assertTrue(MetricsPool.get(MetricIds.PROXY_ROLLBACK_TRANSACTIONS).isPresent());
        assertThat(((FixtureWrapper) MetricsPool.get(MetricIds.PROXY_ROLLBACK_TRANSACTIONS).get()).getFixtureValue(), is(1D));
    }
}
