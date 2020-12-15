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

package org.apache.shardingsphere.agent.plugin.trace;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.plugin.PluginDefinition;

/**
 * Jaeger plugin definition.
 */
public class JaegerPluginDefinition extends PluginDefinition {
    
    public JaegerPluginDefinition() {
        super("jaeger");
    }
    
    @Override
    protected void define() {
        registerService(JaegerTracerService.class);
        
        intercept("org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask")
                .aroundInstanceMethod(ElementMatchers.named("executeCommand"))
                .implement("org.apache.shardingsphere.agent.plugin.trace.advice.CommandExecutorTaskAdvice")
                .build();
        intercept("org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine")
                .aroundInstanceMethod(ElementMatchers.named("parse"))
                .implement("org.apache.shardingsphere.agent.plugin.trace.advice.SQLParserEngineAdvice")
                .build();
        intercept("org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback")
                .aroundInstanceMethod(
                        ElementMatchers.named("execute")
                                .and(ElementMatchers.takesArgument(
                                        0,
                                        ElementMatchers.named("org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit"))
                                )
                )
                .implement("org.apache.shardingsphere.agent.plugin.trace.advice.JDBCExecutorCallbackAdvice")
                .build();
    }
}
