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
import org.apache.shardingsphere.agent.api.advice.TargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.plugin.tracing.zipkin.constant.ZipkinConstants;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.junit.After;
import org.junit.Assert;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class JDBCExecutorCallbackAdviceTest extends AdviceTestBase {
    
    private static Method executeMethod;
    
    private JDBCExecutorCallbackAdvice advice;
    
    private TargetObject targetObject;
    
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
        targetObject = (TargetObject) mock;
        advice = new JDBCExecutorCallbackAdvice();
    }
    
    @Test
    public void testMethod() {
        advice.beforeMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        advice.afterMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        Span span = SPANS.pollFirst();
        Assert.assertNotNull(span);
        Assert.assertEquals("/ShardingSphere/executeSQL/".toLowerCase(), span.name());
        Map<String, String> tags = span.tags();
        Assert.assertFalse(tags == null || tags.isEmpty());
        Assert.assertEquals("shardingsphere", tags.get(ZipkinConstants.Tags.COMPONENT));
        Assert.assertEquals("mock.db", tags.get(ZipkinConstants.Tags.DB_INSTANCE));
        Assert.assertEquals("select 1", tags.get(ZipkinConstants.Tags.DB_STATEMENT));
        Assert.assertEquals("shardingsphere-proxy", tags.get(ZipkinConstants.Tags.DB_TYPE));
        Assert.assertEquals("mock.host", tags.get(ZipkinConstants.Tags.PEER_HOSTNAME));
        Assert.assertEquals("1000", tags.get(ZipkinConstants.Tags.PEER_PORT));
    }
    
    @Test
    public void testExceptionHandle() {
        advice.beforeMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        advice.onThrowing(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new IOException());
        advice.afterMethod(targetObject, executeMethod, new Object[]{executionUnit, false, extraMap}, new MethodInvocationResult());
        Span span = SPANS.pollFirst();
        Assert.assertNotNull(span);
        Assert.assertEquals("/ShardingSphere/executeSQL/".toLowerCase(), span.name());
        Map<String, String> tags = span.tags();
        Assert.assertNotNull(tags);
        Assert.assertEquals("shardingsphere", tags.get(ZipkinConstants.Tags.COMPONENT));
        Assert.assertEquals("mock.db", tags.get(ZipkinConstants.Tags.DB_INSTANCE));
        Assert.assertEquals("select 1", tags.get(ZipkinConstants.Tags.DB_STATEMENT));
        Assert.assertEquals("shardingsphere-proxy", tags.get(ZipkinConstants.Tags.DB_TYPE));
        Assert.assertEquals("mock.host", tags.get(ZipkinConstants.Tags.PEER_HOSTNAME));
        Assert.assertEquals("1000", tags.get(ZipkinConstants.Tags.PEER_PORT));
        Assert.assertEquals("IOException", tags.get("error"));
    }
    
    @After
    public void cleanup() {
        SPANS.clear();
    }
    
}
