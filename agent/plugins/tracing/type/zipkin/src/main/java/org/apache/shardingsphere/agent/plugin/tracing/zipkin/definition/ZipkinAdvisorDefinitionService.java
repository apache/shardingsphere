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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.definition;

import org.apache.shardingsphere.agent.config.advisor.ClassAdvisorConfiguration;
import org.apache.shardingsphere.agent.plugin.tracing.core.advice.TracingAdviceEngine;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice.CommandExecutorTaskAdvice;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice.JDBCExecutorCallbackAdvice;
import org.apache.shardingsphere.agent.spi.advisor.AdvisorDefinitionService;

import java.util.Collection;

/**
 * Zipkin advisor definition service.
 */
public final class ZipkinAdvisorDefinitionService implements AdvisorDefinitionService {
    
    private final TracingAdviceEngine engine = new TracingAdviceEngine(getType());
    
    @Override
    public Collection<ClassAdvisorConfiguration> getProxyAdvisorConfigurations() {
        return engine.getProxyAdvisorConfigurations(CommandExecutorTaskAdvice.class, CommandExecutorTaskAdvice.class, JDBCExecutorCallbackAdvice.class);
    }
    
    @Override
    public Collection<ClassAdvisorConfiguration> getJDBCAdvisorConfigurations() {
        return engine.getJDBCAdvisorConfigurations();
    }
    
    @Override
    public String getType() {
        return "Zipkin";
    }
}
