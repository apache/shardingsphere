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
import org.apache.shardingsphere.agent.metrics.api.constant.MethodNameConstant;
import org.apache.shardingsphere.agent.metrics.api.util.ReflectiveUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class TransactionAdviceTest extends MetricsAdviceBaseTest {
    
    private final TransactionAdvice transactionAdvice = new TransactionAdvice();
    
    @Mock
    private Method commit;
    
    @Mock
    private Method rollback;
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertMethod() {
        when(commit.getName()).thenReturn(MethodNameConstant.COMMIT);
        when(rollback.getName()).thenReturn(MethodNameConstant.ROLL_BACK);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        transactionAdvice.beforeMethod(targetObject, commit, new Object[]{}, new MethodInvocationResult());
        transactionAdvice.beforeMethod(targetObject, rollback, new Object[]{}, new MethodInvocationResult());
        Map<String, DoubleAdder> doubleAdderMap = (Map<String, DoubleAdder>) ReflectiveUtil.getFieldValue(getFixturemetricsregister(), "COUNTER_MAP");
        DoubleAdder commitTotal = doubleAdderMap.get("proxy_transaction_commit_total");
        assertNotNull(commitTotal);
        assertThat(commitTotal.intValue(), is(1));
        DoubleAdder rollbackTotal = doubleAdderMap.get("proxy_transaction_rollback_total");
        assertNotNull(rollbackTotal);
        assertThat(rollbackTotal.intValue(), is(1));
    }
}
