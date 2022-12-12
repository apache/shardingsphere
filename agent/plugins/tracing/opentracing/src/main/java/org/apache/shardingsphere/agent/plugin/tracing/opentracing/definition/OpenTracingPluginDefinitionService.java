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

package org.apache.shardingsphere.agent.plugin.tracing.opentracing.definition;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.definition.PluginDefinitionServiceEngine;
import org.apache.shardingsphere.agent.pointcut.InstanceMethodPointcut;
import org.apache.shardingsphere.agent.spi.PluginDefinitionService;

/**
 * Open tracing plugin definition service.
 */
public final class OpenTracingPluginDefinitionService implements PluginDefinitionService {
    
    private static final String COMMAND_EXECUTOR_TASK_ENHANCE_CLASS = "org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask";
    
    private static final String COMMAND_EXECUTOR_METHOD_NAME = "run";
    
    private static final String COMMAND_EXECUTOR_TASK_ADVICE_CLASS = "org.apache.shardingsphere.agent.plugin.tracing.opentracing.advice.CommandExecutorTaskAdvice";
    
    private static final String SQL_PARSER_ENGINE_ENHANCE_CLASS = "org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine";
    
    private static final String SQL_PARSER_ENGINE_METHOD_NAME = "parse";
    
    private static final String SQL_PARSER_ENGINE_ADVICE_CLASS = "org.apache.shardingsphere.agent.plugin.tracing.opentracing.advice.SQLParserEngineAdvice";
    
    private static final String JDBC_EXECUTOR_CALLBACK_ENGINE_ENHANCE_CLASS = "org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback";
    
    private static final String JDBC_EXECUTOR_METHOD_NAME = "execute";
    
    private static final String JDBC_EXECUTOR_UNIT_ENGINE_ENHANCE_CLASS = "org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit";
    
    private static final String JDBC_EXECUTOR_CALLBACK_ADVICE_CLASS = "org.apache.shardingsphere.agent.plugin.tracing.opentracing.advice.JDBCExecutorCallbackAdvice";
    
    @Override
    public void installProxyInterceptors() {
        PluginDefinitionServiceEngine engine = new PluginDefinitionServiceEngine(this);
        engine.getClassPointcuts(COMMAND_EXECUTOR_TASK_ENHANCE_CLASS).getInstanceMethodPointcuts()
                .add(new InstanceMethodPointcut(ElementMatchers.named(COMMAND_EXECUTOR_METHOD_NAME), COMMAND_EXECUTOR_TASK_ADVICE_CLASS));
        engine.getClassPointcuts(SQL_PARSER_ENGINE_ENHANCE_CLASS).getInstanceMethodPointcuts()
                .add(new InstanceMethodPointcut(ElementMatchers.named(SQL_PARSER_ENGINE_METHOD_NAME), SQL_PARSER_ENGINE_ADVICE_CLASS));
        engine.getClassPointcuts(JDBC_EXECUTOR_CALLBACK_ENGINE_ENHANCE_CLASS).getInstanceMethodPointcuts()
                .add(new InstanceMethodPointcut(ElementMatchers.named(JDBC_EXECUTOR_METHOD_NAME).and(ElementMatchers.takesArgument(0, ElementMatchers.named(JDBC_EXECUTOR_UNIT_ENGINE_ENHANCE_CLASS))),
                        JDBC_EXECUTOR_CALLBACK_ADVICE_CLASS));
    }
    
    @Override
    public void installJdbcInterceptors() {
        // TODO add JDBC related interception
    }
    
    @Override
    public String getType() {
        return "OpenTracing";
    }
}
