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

package org.apache.shardingsphere.agent.plugin.tracing.core.advice;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.core.definition.PluginDefinitionServiceEngine;
import org.apache.shardingsphere.agent.core.plugin.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.adviser.impl.CommandExecutorTaskAdviser;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.adviser.impl.JDBCExecutorCallbackAdviser;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.adviser.impl.SQLParserEngineAdviser;

/**
 * Tracing advice engine.
 */
@RequiredArgsConstructor
public final class TracingAdviceEngine {
    
    private final PluginDefinitionServiceEngine engine;
    
    /**
     * Advice proxy tracing.
     * 
     * @param commandExecutorTaskAdvice command executor task advice
     * @param sqlParserEngineAdvice SQL parser engine advice
     * @param jdbcExecutorCallbackAdvice JDBC executor callback advice
     */
    public void adviceProxyTracing(final Class<? extends InstanceMethodAroundAdvice> commandExecutorTaskAdvice,
                                   final Class<? extends InstanceMethodAroundAdvice> sqlParserEngineAdvice, final Class<? extends InstanceMethodAroundAdvice> jdbcExecutorCallbackAdvice) {
        new CommandExecutorTaskAdviser(engine).advice(commandExecutorTaskAdvice);
        new SQLParserEngineAdviser(engine).advice(sqlParserEngineAdvice);
        new JDBCExecutorCallbackAdviser(engine).advice(jdbcExecutorCallbackAdvice);
    }
    
    /**
     * Advice JDBC tracing.
     */
    public void adviceJDBCTracing() {
        // TODO
    }
}
