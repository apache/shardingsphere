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
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.plugin.tracing.AgentRunner;
import org.apache.shardingsphere.agent.plugin.tracing.MockDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.FieldReader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AgentRunner.class)
public abstract class AbstractJDBCExecutorCallbackAdviceTest implements AdviceTestBase {
    
    @Getter
    private AdviceTargetObject targetObject;
    
    private Object attachment;
    
    @Getter
    private JDBCExecutionUnit executionUnit;
    
    @Getter
    private Map<String, Object> extraMap;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @SneakyThrows({ReflectiveOperationException.class, SQLException.class})
    @Override
    public void prepare() {
        extraMap = new HashMap<>();
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getURL()).thenReturn("mock_url");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(statement.getConnection()).thenReturn(connection);
        executionUnit = new JDBCExecutionUnit(new ExecutionUnit("mock.db", new SQLUnit("select 1", Collections.emptyList())), null, statement);
        JDBCExecutorCallback mock = mock(JDBCExecutorCallback.class, invocation -> {
            switch (invocation.getMethod().getName()) {
                case "getAttachment":
                    return attachment;
                case "setAttachment":
                    attachment = invocation.getArguments()[0];
                    return null;
                default:
                    return invocation.callRealMethod();
            }
        });
        Map<String, DataSourceMetaData> map = (Map<String, DataSourceMetaData>) new FieldReader(mock, JDBCExecutorCallback.class.getDeclaredField("CACHED_DATASOURCE_METADATA")).read();
        map.put("mock_url", new MockDataSourceMetaData());
        targetObject = (AdviceTargetObject) mock;
    }
}
