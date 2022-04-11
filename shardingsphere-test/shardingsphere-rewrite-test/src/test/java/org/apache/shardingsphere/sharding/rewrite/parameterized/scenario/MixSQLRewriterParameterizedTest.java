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
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    protected Map<String, ShardingSphereSchema> mockSchemas() {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_bak", "t_account_detail"));
        TableMetaData accountTableMetaData = mock(TableMetaData.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        Map<String, IndexMetaData> indexMetaDataMap = new HashMap<>(1, 1);
        indexMetaDataMap.put("index_name", new IndexMetaData("index_name"));
        when(accountTableMetaData.getIndexes()).thenReturn(indexMetaDataMap);
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.get("t_account")).thenReturn(accountTableMetaData);
        TableMetaData accountBakTableMetaData = mock(TableMetaData.class);
        when(accountBakTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(result.containsTable("t_account_bak")).thenReturn(true);
        when(result.get("t_account_bak")).thenReturn(accountBakTableMetaData);
        when(result.get("t_account_detail")).thenReturn(mock(TableMetaData.class));
        when(result.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        when(result.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        return Collections.singletonMap(DefaultSchema.LOGIC_NAME, result);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules) {
    }
    
    @Override
    protected void mockDataSource(final Map<String, DataSource> dataSources) throws SQLException {
    }

    private Map<String, ColumnMetaData> createColumnMetaDataMap() {
        Map<String, ColumnMetaData> result = new LinkedHashMap<>(4, 1);
        result.put("account_id", new ColumnMetaData("account_id", Types.INTEGER, true, true, false));
        result.put("password", mock(ColumnMetaData.class));
        result.put("amount", mock(ColumnMetaData.class));
        result.put("status", mock(ColumnMetaData.class));
        return result;
    }
}
