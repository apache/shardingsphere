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

package org.apache.shardingsphere.test.it.rewrite.engine.scenario;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.it.rewrite.engine.SQLRewriterIT;
import org.apache.shardingsphere.test.it.rewrite.engine.SQLRewriterITSettings;
import org.apache.shardingsphere.test.it.rewrite.engine.parameter.SQLRewriteEngineTestParameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SQLRewriterITSettings("scenario/encrypt/case")
class EncryptSQLRewriterIT extends SQLRewriterIT {
    
    @Override
    protected YamlRootConfiguration createRootConfiguration(final SQLRewriteEngineTestParameters testParams) throws IOException {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(testParams.getRuleFile()), "Can not find rewrite rule yaml configuration");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    @Override
    protected Map<String, ShardingSphereSchema> mockSchemas(final String schemaName) {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        when(result.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        when(result.getAllColumnNames("t_account_detail")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount", "status"));
        when(result.getAllColumnNames("t_order")).thenReturn(Arrays.asList("ORDER_ID", "USER_ID", "CONTENT"));
        when(result.getVisibleColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount"));
        when(result.getVisibleColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount"));
        when(result.getVisibleColumnNames("t_account_detail")).thenReturn(Arrays.asList("account_id", "certificate_number", "password", "amount"));
        when(result.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("ORDER_ID", "USER_ID", "CONTENT"));
        when(result.getTable("t_account")).thenReturn(new ShardingSphereTable("t_account", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        when(result.getTable("t_account_bak")).thenReturn(new ShardingSphereTable("t_account_bak", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        when(result.getTable("t_account_detail")).thenReturn(new ShardingSphereTable("t_account_detail", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        when(result.getTable("t_order")).thenReturn(new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.containsTable("t_account_bak")).thenReturn(true);
        when(result.containsTable("t_account_detail")).thenReturn(true);
        when(result.containsTable("t_order")).thenReturn(true);
        return Collections.singletonMap(schemaName, result);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules, final String schemaName, final SQLStatement sqlStatement) {
        Optional<SingleRule> singleRule = rules.stream().filter(each -> each instanceof SingleRule).map(each -> (SingleRule) each).findFirst();
        if (singleRule.isPresent() && !(sqlStatement instanceof CreateTableStatement)) {
            singleRule.get().put("encrypt_ds", schemaName, "t_account");
            singleRule.get().put("encrypt_ds", schemaName, "t_account_bak");
            singleRule.get().put("encrypt_ds", schemaName, "t_account_detail");
            singleRule.get().put("encrypt_ds", schemaName, "t_order");
        }
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    protected void mockDataSource(final Map<String, DataSource> dataSources) throws SQLException {
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
            when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/foo_ds");
            when(connection.getMetaData().getUserName()).thenReturn("root");
            when(connection.createStatement(anyInt(), anyInt(), anyInt()).getConnection()).thenReturn(connection);
            ResultSet typeInfo = mockTypeInfo();
            when(connection.getMetaData().getTypeInfo()).thenReturn(typeInfo);
            entry.setValue(new MockedDataSource(connection));
        }
    }
    
    private ResultSet mockTypeInfo() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TYPE_NAME")).thenReturn("INTEGER", "VARCHAR");
        when(result.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        return result;
    }
}
