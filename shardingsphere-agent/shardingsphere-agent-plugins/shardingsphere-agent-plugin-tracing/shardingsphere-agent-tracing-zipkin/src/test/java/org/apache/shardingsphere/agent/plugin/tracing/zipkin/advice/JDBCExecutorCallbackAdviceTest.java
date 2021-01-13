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

package org.apache.shardingsphere.agent.plugin.tracing.zipkin.advice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldReader;
import zipkin2.Span;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JDBCExecutorCallbackAdviceTest extends AdviceBaseTest {
    
    private static Method executeMethod;
    
    private JDBCExecutorCallbackAdvice advice;
    
    private AdviceTargetObject targetObject;
    
    private Object attachment;
    
    private JDBCExecutionUnit executionUnit;
    
    private Map<String, Object> extraMap;
    
    @BeforeClass
    @SneakyThrows
    public static void setup() {
        prepare("org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback");
        executeMethod = JDBCExecutorCallback.class.getDeclaredMethod("execute", JDBCExecutionUnit.class, boolean.class, Map.class);
    }
    
    @Before
    @SneakyThrows
    @SuppressWarnings("all")
    public void before() {
        extraMap = Maps.newHashMap();
        extraMap.put(ZipkinConstants.ROOT_SPAN, null);
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn("mock_url");
        when(connection.getMetaData()).thenReturn(metaData);
        when(statement.getConnection()).thenReturn(connection);
        executionUnit = new JDBCExecutionUnit(new ExecutionUnit("mock.db", new SQLUnit("select 1", Lists.newArrayList())), null, statement);
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
        advice = new JDBCExecutorCallbackAdvice();
    }
    
    @Test
    public void testMethod() {
        advice.beforeMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        advice.afterMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        Span span = SPANS.pollFirst();
        assertNotNull(span);
        assertThat(span.name(), is("/ShardingSphere/executeSQL/".toLowerCase()));
        Map<String, String> tags = span.tags();
        assertFalse(tags == null || tags.isEmpty());
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is("shardingsphere"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_INSTANCE), is("mock.db"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_STATEMENT), is("select 1"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is("shardingsphere-proxy"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_HOSTNAME), is("mock.host"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_PORT), is("1000"));
    }
    
    @Test
    public void testExceptionHandle() {
        advice.beforeMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        advice.onThrowing(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new IOException());
        advice.afterMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        Span span = SPANS.pollFirst();
        assertNotNull(span);
        assertThat(span.name(), is("/ShardingSphere/executeSQL/".toLowerCase()));
        Map<String, String> tags = span.tags();
        assertNotNull(tags);
        assertThat(tags.get(ZipkinConstants.Tags.COMPONENT), is("shardingsphere"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_INSTANCE), is("mock.db"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_STATEMENT), is("select 1"));
        assertThat(tags.get(ZipkinConstants.Tags.DB_TYPE), is("shardingsphere-proxy"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_HOSTNAME), is("mock.host"));
        assertThat(tags.get(ZipkinConstants.Tags.PEER_PORT), is("1000"));
        assertThat(tags.get("error"), is("IOException"));
    }
    
    @After
    public void cleanup() {
        SPANS.clear();
    }
}
