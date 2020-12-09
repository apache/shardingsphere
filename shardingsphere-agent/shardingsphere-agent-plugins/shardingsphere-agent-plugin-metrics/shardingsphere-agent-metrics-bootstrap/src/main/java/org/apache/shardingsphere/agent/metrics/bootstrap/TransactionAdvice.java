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
 *
 */

package org.apache.shardingsphere.agent.metrics.bootstrap;

import java.lang.reflect.Method;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.metrics.api.reporter.MetricsReporter;

/**
 * Transaction advice.
 */
public final class TransactionAdvice implements MethodAroundAdvice {
    
    private static final String COMMIT = "proxy_transaction_commit_total";
    
    private static final String ROLLBACK = "proxy_transaction_rollback_total";
    
    static {
        MetricsReporter.registerCounter(COMMIT, "the shardingsphere proxy transaction commit count total");
        MetricsReporter.registerCounter(ROLLBACK, "the shardingsphere proxy transaction rollback count total");
    }
    
    @Override
    public void beforeMethod(final TargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        String methodName = method.getName();
        if (MethodNameConstant.COMMIT.equals(methodName)) {
            MetricsReporter.counterIncrement(COMMIT);
        } else if (MethodNameConstant.ROLL_BACK.equals(methodName)) {
            MetricsReporter.counterIncrement(ROLLBACK);
        }
    }
}
