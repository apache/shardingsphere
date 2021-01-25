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

package org.apache.shardingsphere.agent.plugin.tracing.advice;

import lombok.Getter;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.AgentRunner;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.runner.RunWith;

@RunWith(AgentRunner.class)
public abstract class AbstractCommandExecutorTaskAdviceTest implements AdviceTestBase {
    
    @Getter
    private AdviceTargetObject targetObject;
    
    @Override
    @SuppressWarnings("all")
    public void prepare() {
        Object executorTask = new CommandExecutorTask(null, new BackendConnection(TransactionType.BASE), null, null);
        targetObject = (AdviceTargetObject) executorTask;
    }
}
