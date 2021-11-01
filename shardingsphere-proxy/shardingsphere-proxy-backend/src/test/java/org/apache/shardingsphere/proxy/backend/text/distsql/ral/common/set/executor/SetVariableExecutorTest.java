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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.executor;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor.SetVariableExecutor;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SetVariableExecutorTest {
    
    @Test
    public void assertExecuteWithTransactionType() {
        SetVariableStatement statement = new SetVariableStatement("transaction_type", "local");
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.XA));
        new SetVariableExecutor(statement, connection).execute();
        Assert.assertThat(TransactionType.LOCAL.name(), is(connection.getTransactionStatus().getTransactionType().name()));
    }
    
    @Test
    public void assertExecuteWithAgent() {
        SetVariableStatement statement = new SetVariableStatement("AGENT_PLUGINS_ENABLED", "false");
        BackendConnection connection = mock(BackendConnection.class);
        new SetVariableExecutor(statement, connection).execute();
        String expectedValue = SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), "default");
        Assert.assertThat(expectedValue, is("false"));
    }
    
    @Test
    public void assertExecuteWithConfigurationKey() {
        SetVariableStatement statement = new SetVariableStatement("proxy_frontend_flush_threshold", "1024");
        BackendConnection connection = mock(BackendConnection.class);
        new SetVariableExecutor(statement, connection).execute();
        String expectedValue = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps().getProps().get("proxy-frontend-flush-threshold").toString();
        Assert.assertThat(expectedValue, is("1024"));
    }
}
