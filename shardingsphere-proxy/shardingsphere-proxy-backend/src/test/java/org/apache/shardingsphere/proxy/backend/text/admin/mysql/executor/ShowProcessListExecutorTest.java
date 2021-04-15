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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowProcessListExecutorTest {
    
    private static final String SCHEMA_NAME = "sharding_db";
    
    private ShowProcessListExecutor showProcessListExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        showProcessListExecutor = new ShowProcessListExecutor();
        setupMetaDataContexts();
        setupChildrenValues();
    }
    
    private void setupMetaDataContexts() throws NoSuchFieldException, IllegalAccessException {
        Field metaDataContextsField = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContextsField.setAccessible(true);
        Map<String, ShardingSphereMetaData> metaDataMap = getMetaDataMap();
        MetaDataContexts metaDataContexts = new StandardMetaDataContexts(metaDataMap, mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class),
            new ShardingSphereUsers(Collections.singleton(new ShardingSphereUser("root", "root", ""))), new ConfigurationProperties(new Properties()));
        metaDataContextsField.set(ProxyContext.getInstance(), metaDataContexts);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(2);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.isComplete()).thenReturn(true);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        result.put(SCHEMA_NAME, metaData);
        return result;
    }
    
    private void setupChildrenValues() throws NoSuchFieldException, IllegalAccessException {
        Field childrenValuesField = showProcessListExecutor.getClass().getDeclaredField("processListData");
        childrenValuesField.setAccessible(true);
        String executionNodeValue = "executionID: f6c2336a-63ba-41bf-941e-2e3504eb2c80\n"
            + "startTimeMillis: 1617939785160\n"
            + "unitStatuses:\n"
            + "- status: EXECUTE_STATUS_START\n"
            + "  unitID: unitID1\n"
            + "- status: EXECUTE_STATUS_DONE\n"
            + "  unitID: unitID2\n";
        childrenValuesField.set(showProcessListExecutor, Collections.singleton(executionNodeValue));
    }
    
    @Test
    public void assertExecute() throws SQLException {
        showProcessListExecutor.execute(mockBackendConnection());
        assertThat(showProcessListExecutor.getQueryResultMetaData().getColumnCount(), is(8));
        MergedResult mergedResult = showProcessListExecutor.getMergedResult();
        while (mergedResult.next()) {
            assertThat(mergedResult.getValue(1, String.class), is("f6c2336a-63ba-41bf-941e-2e3504eb2c80"));
            assertThat(mergedResult.getValue(2, String.class), is("root"));
            assertThat(mergedResult.getValue(3, String.class), is("localhost:30000"));
            assertThat(mergedResult.getValue(4, String.class), is(SCHEMA_NAME));
            assertThat(mergedResult.getValue(7, String.class), is("Executing 1/2"));
        }
    }
    
    private BackendConnection mockBackendConnection() {
        BackendConnection result = mock(BackendConnection.class);
        when(result.getGrantee()).thenReturn(new Grantee("root", "localhost:30000"));
        when(result.getSchemaName()).thenReturn(SCHEMA_NAME);
        return result;
    }
}
