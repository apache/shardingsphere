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
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MixSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String CASE_PATH = "scenario/mix/case";
    
    public MixSQLRewriterParameterizedTest(final String type, final String name, final String fileName,
                                           final String databaseType, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} ({3}) -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(CASE_PATH.toUpperCase(), CASE_PATH, MixSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected YamlRootConfiguration createRootConfiguration() throws IOException {
        URL url = MixSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configurations.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    @Override
    protected Map<String, ShardingSphereSchema> mockSchemas(final String schemaName) {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_bak", "t_account_detail"));
        ShardingSphereTable accountTable = mock(ShardingSphereTable.class);
        when(accountTable.getColumns()).thenReturn(createColumns());
        when(accountTable.getIndexes()).thenReturn(Collections.singletonMap("index_name", new ShardingSphereIndex("index_name")));
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.getTable("t_account")).thenReturn(accountTable);
        ShardingSphereTable accountBakTable = mock(ShardingSphereTable.class);
        when(accountBakTable.getColumns()).thenReturn(createColumns());
        when(result.containsTable("t_account_bak")).thenReturn(true);
        when(result.getTable("t_account_bak")).thenReturn(accountBakTable);
        when(result.getTable("t_account_detail")).thenReturn(mock(ShardingSphereTable.class));
        when(result.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        when(result.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        when(result.getVisibleColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "password", "amount"));
        when(result.getVisibleColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "password", "amount"));
        return Collections.singletonMap(schemaName, result);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules, final String schemaName) {
    }
    
    @Override
    protected void mockDataSource(final Map<String, DataSource> dataSources) {
    }
    
    private Map<String, ShardingSphereColumn> createColumns() {
        Map<String, ShardingSphereColumn> result = new LinkedHashMap<>(4, 1);
        result.put("account_id", new ShardingSphereColumn("account_id", Types.INTEGER, true, true, false, true));
        result.put("password", mock(ShardingSphereColumn.class));
        result.put("amount", mock(ShardingSphereColumn.class));
        result.put("status", mock(ShardingSphereColumn.class));
        return result;
    }
}
