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

import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsPool;
import org.apache.shardingsphere.agent.plugin.metrics.core.MetricsWrapper;
import org.apache.shardingsphere.agent.plugin.metrics.core.constant.MetricIds;

import java.lang.reflect.Method;

/**
 * Requests count advice for ShardingSphere-Proxy.
 */
public final class RequestsCountAdvice implements InstanceMethodAdvice {
    
    static {
        MetricsPool.create(MetricIds.PROXY_REQUESTS);
    }
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args) {
        MetricsPool.get(MetricIds.PROXY_REQUESTS).ifPresent(MetricsWrapper::inc);
    }
}
