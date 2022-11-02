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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TransactionAdviceTest extends MetricsAdviceBaseTest {
    
    private final TransactionAdvice transactionAdvice = new TransactionAdvice();
    
    @Mock
    private Method commit;
    
    @Mock
    private Method rollback;
    
    @Test
    public void assertMethod() {
        when(commit.getName()).thenReturn(TransactionAdvice.COMMIT);
        when(rollback.getName()).thenReturn(TransactionAdvice.ROLLBACK);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        transactionAdvice.beforeMethod(targetObject, commit, new Object[]{}, new MethodInvocationResult());
        transactionAdvice.beforeMethod(targetObject, rollback, new Object[]{}, new MethodInvocationResult());
        FixtureWrapper commitWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.TRANSACTION_COMMIT).get();
        assertTrue(MetricsPool.get(MetricIds.TRANSACTION_COMMIT).isPresent());
        assertThat(commitWrapper.getFixtureValue(), is(1.0));
        FixtureWrapper rollbackWrapper = (FixtureWrapper) MetricsPool.get(MetricIds.TRANSACTION_ROLLBACK).get();
        assertTrue(MetricsPool.get(MetricIds.TRANSACTION_ROLLBACK).isPresent());
        assertThat(rollbackWrapper.getFixtureValue(), is(1.0));
    }
}
