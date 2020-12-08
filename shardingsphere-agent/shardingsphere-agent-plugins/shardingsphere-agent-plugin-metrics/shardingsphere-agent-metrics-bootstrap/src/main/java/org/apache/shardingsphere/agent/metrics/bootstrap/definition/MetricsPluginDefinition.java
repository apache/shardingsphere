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

package org.apache.shardingsphere.agent.metrics.bootstrap.definition;

import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.plugin.PluginDefinition;
import org.apache.shardingsphere.agent.metrics.api.MetricsProvider;
import org.apache.shardingsphere.agent.metrics.bootstrap.MethodNameConstant;

/**
 * Metrics plugin definition.
 */
public final class MetricsPluginDefinition extends PluginDefinition {
    
    private static final String COMMAND_EXECUTOR_TASK_ENHANCE_CLASS = "org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask";
    
    private static final String COMMAND_EXECUTOR_TASK_ADVICE_CLASS = "org.apache.shardingsphere.agent.metrics.bootstrap.CommandExecutorTaskAdvice";
    
    private static final String CHANNEL_HANDLER_ENHANCE_CLASS = "org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler";
    
    private static final String CHANNEL_HANDLER_ADVICE_CLASS = "org.apache.shardingsphere.agent.metrics.bootstrap.ChannelHandlerAdvice";
    
    private static final String DATA_NODE_ROUTER_ENHANCE_CLASS = "org.apache.shardingsphere.infra.route.DataNodeRouter";
    
    private static final String DATA_NODE_ROUTER_ADVICE_CLASS = "org.apache.shardingsphere.agent.metrics.bootstrap.DataNodeRouterAdvice";
    
    private static final String TRANSACTION_ENHANCE_CLASS = "org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager";
    
    private static final String TRANSACTION_ADVICE_CLASS = "org.apache.shardingsphere.agent.metrics.bootstrap.TransactionAdvice";
    
    public MetricsPluginDefinition() {
        super("Metrics");
    }
    
    @Override
    protected void define() {
        intercept(COMMAND_EXECUTOR_TASK_ENHANCE_CLASS)
                .aroundInstanceMethod(ElementMatchers.named("run"))
                .implement(COMMAND_EXECUTOR_TASK_ADVICE_CLASS)
                .build();
        intercept(CHANNEL_HANDLER_ENHANCE_CLASS)
                .aroundInstanceMethod(ElementMatchers.named(MethodNameConstant.CHANNEL_ACTIVE).or(ElementMatchers.named(MethodNameConstant.CHANNEL_INACTIVE))
                        .or(ElementMatchers.named(MethodNameConstant.CHANNEL_READ)))
                .implement(CHANNEL_HANDLER_ADVICE_CLASS)
                .build();
        intercept(DATA_NODE_ROUTER_ENHANCE_CLASS)
                .aroundInstanceMethod(ElementMatchers.named("route"))
                .implement(DATA_NODE_ROUTER_ADVICE_CLASS)
                .build();
        intercept(TRANSACTION_ENHANCE_CLASS)
                .aroundInstanceMethod(ElementMatchers.named(MethodNameConstant.COMMIT).or(ElementMatchers.named(MethodNameConstant.ROLL_BACK)))
                .implement(TRANSACTION_ADVICE_CLASS)
                .build();
        MetricsProvider.INSTANCE.newInstance();
    }
}
