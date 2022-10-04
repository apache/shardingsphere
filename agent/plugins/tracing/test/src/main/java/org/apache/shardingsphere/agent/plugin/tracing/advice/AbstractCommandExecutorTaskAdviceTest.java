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

import io.netty.util.DefaultAttributeMap;
import lombok.Getter;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.AgentRunner;
import org.apache.shardingsphere.agent.plugin.tracing.ProxyContextRestorer;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@RunWith(AgentRunner.class)
public abstract class AbstractCommandExecutorTaskAdviceTest extends ProxyContextRestorer implements AdviceTestBase {
    
    @Getter
    private AdviceTargetObject targetObject;
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public final void prepare() {
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        ConnectionSession connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.BASE, new DefaultAttributeMap());
        Object executorTask = new CommandExecutorTask(null, connectionSession, null, null);
        targetObject = (AdviceTargetObject) executorTask;
    }
}
