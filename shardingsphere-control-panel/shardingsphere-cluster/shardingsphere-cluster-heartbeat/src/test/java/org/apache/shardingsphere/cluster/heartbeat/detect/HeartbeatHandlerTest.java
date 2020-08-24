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

package org.apache.shardingsphere.cluster.heartbeat.detect;

import lombok.SneakyThrows;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResult;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HeartbeatHandlerTest {
    
    private static final String DETECT_SQL = "select 1";
    
    private static final String SCHEMA_NAME = "sharding_db";
    
    private static final String DATA_SOURCE_0 = "ds_0";
    
    private static final String DATA_SOURCE_1 = "ds_1";
    
    private static boolean enableExecuteQuery;
    
    private static boolean multipleDataSource;
    
    private HeartbeatHandler handler;
    
    @Before
    public void init() {
        handler = HeartbeatHandler.getInstance();
        enableExecuteQuery = true;
        multipleDataSource = false;
    }
    
    @Test
    public void assertHandleWithoutRetry() {
        handler.init(getHeartbeatConfiguration(false));
        HeartbeatResponse response = handler.handle(getSchemaContext(), Collections.emptyList());
        assertNotNull(response);
        assertNotNull(response.getHeartbeatResultMap());
        assertTrue(response.getHeartbeatResultMap().containsKey(SCHEMA_NAME));
        assertThat(response.getHeartbeatResultMap().get(SCHEMA_NAME).size(), is(1));
        HeartbeatResult heartbeatResult = response.getHeartbeatResultMap().get(SCHEMA_NAME).iterator().next();
        assertNotNull(heartbeatResult);
        assertThat(heartbeatResult.getDataSourceName(), is(DATA_SOURCE_0));
        assertTrue(heartbeatResult.isEnable());
    }
    
    @Test
    public void assertHandleWhenDetectExceptionWithoutRetry() {
        enableExecuteQuery = false;
        handler.init(getHeartbeatConfiguration(false));
        HeartbeatResponse response = handler.handle(getSchemaContext(), Collections.emptyList());
        assertNotNull(response);
        assertNotNull(response.getHeartbeatResultMap());
        assertTrue(response.getHeartbeatResultMap().containsKey(SCHEMA_NAME));
        assertThat(response.getHeartbeatResultMap().get(SCHEMA_NAME).size(), is(1));
        HeartbeatResult heartbeatResult = response.getHeartbeatResultMap().get(SCHEMA_NAME).iterator().next();
        assertNotNull(heartbeatResult);
        assertThat(heartbeatResult.getDataSourceName(), is(DATA_SOURCE_0));
        assertFalse(heartbeatResult.isEnable());
    }
    
    @Test
    public void assertHandleWithRetry() {
        handler.init(getHeartbeatConfiguration(true));
        HeartbeatResponse response = handler.handle(getSchemaContext(), Collections.emptyList());
        assertNotNull(response);
        assertNotNull(response.getHeartbeatResultMap());
        assertTrue(response.getHeartbeatResultMap().containsKey(SCHEMA_NAME));
        assertThat(response.getHeartbeatResultMap().get(SCHEMA_NAME).size(), is(1));
        HeartbeatResult heartbeatResult = response.getHeartbeatResultMap().get(SCHEMA_NAME).iterator().next();
        assertNotNull(heartbeatResult);
        assertThat(heartbeatResult.getDataSourceName(), is(DATA_SOURCE_0));
        assertTrue(heartbeatResult.isEnable());
    }
    
    @Test
    public void assertHandleWhenDetectExceptionWithRetry() {
        enableExecuteQuery = false;
        handler.init(getHeartbeatConfiguration(true));
        HeartbeatResponse response = handler.handle(getSchemaContext(), Collections.emptyList());
        assertNotNull(response);
        assertNotNull(response.getHeartbeatResultMap());
        assertTrue(response.getHeartbeatResultMap().containsKey(SCHEMA_NAME));
        assertThat(response.getHeartbeatResultMap().get(SCHEMA_NAME).size(), is(1));
        HeartbeatResult heartbeatResult = response.getHeartbeatResultMap().get(SCHEMA_NAME).iterator().next();
        assertNotNull(heartbeatResult);
        assertThat(heartbeatResult.getDataSourceName(), is(DATA_SOURCE_0));
        assertFalse(heartbeatResult.isEnable());
    }
    
    @Test
    public void assertMultipleDataSource() {
        multipleDataSource = true;
        handler.init(getHeartbeatConfiguration(false));
        HeartbeatResponse response = handler.handle(getSchemaContext(), Collections.emptyList());
        assertNotNull(response);
        assertNotNull(response.getHeartbeatResultMap());
        assertTrue(response.getHeartbeatResultMap().containsKey(SCHEMA_NAME));
        assertThat(response.getHeartbeatResultMap().get(SCHEMA_NAME).size(), is(2));
        assertTrue(response.getHeartbeatResultMap().get(SCHEMA_NAME).stream().map(HeartbeatResult::getDataSourceName)
                .collect(Collectors.toList()).containsAll(Arrays.asList(DATA_SOURCE_0, DATA_SOURCE_1)));
        response.getHeartbeatResultMap().get(SCHEMA_NAME).iterator().forEachRemaining(each -> assertTrue(each.isEnable()));
    }
    
    @Test
    public void assertHandleWithDisableDataSource() {
        handler.init(getHeartbeatConfiguration(true));
        HeartbeatResponse response = handler.handle(getSchemaContext(), Arrays.asList("sharding_db.ds_0"));
        assertNotNull(response);
        assertNotNull(response.getHeartbeatResultMap());
        assertTrue(response.getHeartbeatResultMap().containsKey(SCHEMA_NAME));
        assertThat(response.getHeartbeatResultMap().get(SCHEMA_NAME).size(), is(1));
        HeartbeatResult heartbeatResult = response.getHeartbeatResultMap().get(SCHEMA_NAME).iterator().next();
        assertNotNull(heartbeatResult);
        assertThat(heartbeatResult.getDataSourceName(), is(DATA_SOURCE_0));
        assertFalse(heartbeatResult.isEnable());
        assertTrue(heartbeatResult.isDisabled());
    }
    
    private HeartbeatConfiguration getHeartbeatConfiguration(final boolean retry) {
        HeartbeatConfiguration result = new HeartbeatConfiguration();
        result.setSql(DETECT_SQL);
        result.setThreadCount(50);
        result.setInterval(10);
        result.setRetryEnable(retry);
        result.setRetryMaximum(3);
        result.setRetryInterval(1);
        return result;
    }
    
    private Map<String, SchemaContext> getSchemaContext() {
        Map<String, SchemaContext> result = new HashMap<>(1, 1);
        SchemaContext schemaContext = mock(SchemaContext.class);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schemaContext.getSchema()).thenReturn(schema);
        Map<String, DataSource> dataSources = getDataSources();
        when(schema.getDataSources()).thenReturn(dataSources);
        result.put(SCHEMA_NAME, schemaContext);
        return result;
    }
    
    private Map<String, DataSource> getDataSources() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put(DATA_SOURCE_0, getDataSource());
        if (multipleDataSource) {
            result.put(DATA_SOURCE_1, getDataSource());
        }
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private DataSource getDataSource() {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = getStatement();
        when(result.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DETECT_SQL)).thenReturn(preparedStatement);
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private PreparedStatement getStatement() {
        PreparedStatement result = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        if (enableExecuteQuery) {
            when(result.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
        }
        return result;
    }
    
    @After
    public void close() {
        handler.close();
    }
}
