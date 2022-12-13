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

package org.apache.shardingsphere.agent.plugin.tracing.jaeger.definition;

import org.apache.shardingsphere.agent.core.definition.PluginDefinitionServiceEngine;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingAdviceEngine;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.advice.CommandExecutorTaskAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.jaeger.advice.JDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.spi.PluginDefinitionService;

/**
 * Jaeger plugin definition service.
 */
public final class JaegerPluginDefinitionService implements PluginDefinitionService {
    
    @Override
    public void installProxyInterceptors() {
        new TracingAdviceEngine(new PluginDefinitionServiceEngine(this)).adviceProxyTracing(CommandExecutorTaskAdvice.class, CommandExecutorTaskAdvice.class, JDBCExecutorCallbackAdvice.class);
    }
    
    @Override
    public void installJdbcInterceptors() {
        new TracingAdviceEngine(new PluginDefinitionServiceEngine(this)).adviceJDBCTracing();
    }
    
    @Override
    public String getType() {
        return "Jaeger";
    }
}
