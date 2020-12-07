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

/**
 * Command executor task plugin definition.
 */
public final class CommandExecutorTaskPluginDefinition extends PluginDefinition {
    
    private static final String ENHANCE_CLASS = "org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask";
    
    private static final String COMMAND_EXECUTOR_TASK_CLASS = "org.apache.shardingsphere.agent.metrics.bootstrap.CommandExecutorTaskAdvice";

    public MetricsPluginDefinition() {
        super("sample-metrics");
    }

    @Override
    protected void define() {
        intercept(ENHANCE_CLASS)
                .aroundInstanceMethod(ElementMatchers.named("run"))
                .implement(COMMAND_EXECUTOR_TASK_CLASS)
                .build();
    }
}
