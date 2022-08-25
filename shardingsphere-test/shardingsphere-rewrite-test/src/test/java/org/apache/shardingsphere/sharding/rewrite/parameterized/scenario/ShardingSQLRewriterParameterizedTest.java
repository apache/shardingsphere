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
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String CASE_PATH = "scenario/sharding/case";
    
    public ShardingSQLRewriterParameterizedTest(final String type, final String name, final String fileName,
                                                final String databaseType, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} ({3}) -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(CASE_PATH.toUpperCase(), CASE_PATH, ShardingSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected YamlRootConfiguration createRootConfiguration() throws IOException {
        URL url = ShardingSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules, final String schemaName) {
        Optional<SingleTableRule> singleTableRule = rules.stream().filter(each -> each instanceof SingleTableRule).map(each -> (SingleTableRule) each).findFirst();
        if (singleTableRule.isPresent()) {
            singleTableRule.get().put("db", schemaName, "t_single");
            singleTableRule.get().put("db", schemaName, "t_single_extend");
        }
    }
    
    @Override
    protected Map<String, ShardingSphereSchema> mockSchemas(final String schemaName) {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_detail"));
        ShardingSphereTable accountTableMetaData = mock(ShardingSphereTable.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(accountTableMetaData.getIndexes()).thenReturn(Collections.singletonMap("status_idx_exist", new ShardingSphereIndex("status_idx_exist")));
        when(accountTableMetaData.getPrimaryKeyColumns()).thenReturn(Collections.singletonList("account_id"));
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.get("t_account")).thenReturn(accountTableMetaData);
        when(result.get("t_account_detail")).thenReturn(mock(ShardingSphereTable.class));
        when(result.getAllColumnNames("t_account")).thenReturn(new ArrayList<>(Arrays.asList("account_id", "amount", "status")));
        when(result.getAllColumnNames("t_user")).thenReturn(new ArrayList<>(Arrays.asList("id", "content")));
        when(result.getAllColumnNames("t_user_extend")).thenReturn(new ArrayList<>(Arrays.asList("user_id", "content")));
        when(result.getVisibleColumnNames("t_account")).thenReturn(new ArrayList<>(Arrays.asList("account_id", "amount")));
        when(result.getVisibleColumnNames("t_user")).thenReturn(new ArrayList<>(Arrays.asList("id", "content")));
        when(result.getVisibleColumnNames("t_user_extend")).thenReturn(new ArrayList<>(Arrays.asList("user_id", "content")));
        when(result.containsColumn("t_account", "account_id")).thenReturn(true);
        return Collections.singletonMap(schemaName, result);
    }
    
    private Map<String, ShardingSphereColumn> createColumnMetaDataMap() {
        Map<String, ShardingSphereColumn> result = new LinkedHashMap<>(3, 1);
        result.put("account_id", new ShardingSphereColumn("account_id", Types.INTEGER, true, true, false, true));
        result.put("amount", mock(ShardingSphereColumn.class));
        result.put("status", mock(ShardingSphereColumn.class));
        return result;
    }
    
    @Override
    protected void mockDataSource(final Map<String, DataSource> dataSources) {
    }
}
