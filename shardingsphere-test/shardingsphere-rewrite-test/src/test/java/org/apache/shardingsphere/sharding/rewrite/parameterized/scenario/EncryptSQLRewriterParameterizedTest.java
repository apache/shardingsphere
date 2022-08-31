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

package org.apache.shardingsphere.sharding.rewrite.parameterized.scenario;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String CASE_PATH = "scenario/encrypt/case";
    
    public EncryptSQLRewriterParameterizedTest(final String type, final String name, final String fileName, final String databaseType, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} ({3}) -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(CASE_PATH.toUpperCase(), CASE_PATH, EncryptSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected YamlRootConfiguration createRootConfiguration() throws IOException {
        URL url = EncryptSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
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
        when(result.getTable("t_order")).thenReturn(new ShardingSphereTable("t_order", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        return Collections.singletonMap(schemaName, result);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules, final String schemaName) {
        Optional<SingleTableRule> singleTableRule = rules.stream().filter(each -> each instanceof SingleTableRule).map(each -> (SingleTableRule) each).findFirst();
        if (singleTableRule.isPresent()) {
            singleTableRule.get().put("encrypt_ds", schemaName, "t_account");
            singleTableRule.get().put("encrypt_ds", schemaName, "t_account_bak");
            singleTableRule.get().put("encrypt_ds", schemaName, "t_account_detail");
            singleTableRule.get().put("encrypt_ds", schemaName, "t_order");
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
        when(result.getInt("DATA_TYPE")).thenReturn(4, 12);
        return result;
    }
}
