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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.jdbc;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.core.advice.AbstractInstanceMethodAdvice;
import org.apache.shardingsphere.agent.plugin.core.holder.ContextManagerHolder;
import org.apache.shardingsphere.agent.plugin.core.util.AgentReflectionUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.lang.reflect.Method;

/**
 * ShardingSphere data source advice.
 */
public final class ShardingSphereDataSourceAdvice extends AbstractInstanceMethodAdvice {
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        if ("close".equals(method.getName())) {
            ContextManagerHolder.remove(getDatabaseName(target));
        }
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        if ("createContextManager".equals(method.getName())) {
            ContextManagerHolder.put(getDatabaseName(target), (ContextManager) result);
        }
    }
    
    private String getDatabaseName(final TargetAdviceObject target) {
        return AgentReflectionUtils.getFieldValue(target, "databaseName");
    }
}
