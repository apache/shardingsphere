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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.test.it.rewrite.engine.SQLRewriterIT;
import org.apache.shardingsphere.test.it.rewrite.engine.SQLRewriterITSettings;
import org.apache.shardingsphere.test.it.rewrite.engine.parameter.SQLRewriteEngineTestParameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SQLRewriterITSettings("scenario/sharding/case")
class ShardingSQLRewriterIT extends SQLRewriterIT {
    
    @Override
    protected YamlRootConfiguration createRootConfiguration(final SQLRewriteEngineTestParameters testParams) throws IOException {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(testParams.getRuleFile()), "Can not find rewrite rule yaml configuration");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules, final String schemaName, final SQLStatement sqlStatement) {
        Optional<SingleRule> singleRule = rules.stream().filter(each -> each instanceof SingleRule).map(each -> (SingleRule) each).findFirst();
        if (singleRule.isPresent() && !(sqlStatement instanceof CreateTableStatement)) {
            singleRule.get().put("db", schemaName, "t_single");
            singleRule.get().put("db", schemaName, "t_single_extend");
        }
    }
    
    @Override
    protected Map<String, ShardingSphereSchema> mockSchemas(final String schemaName) {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_detail"));
        ShardingSphereTable accountTableMetaData = mock(ShardingSphereTable.class);
        when(accountTableMetaData.getColumnValues()).thenReturn(createColumnMetaDataMap());
        when(accountTableMetaData.getIndexValues()).thenReturn(Collections.singletonList(new ShardingSphereIndex("status_idx_exist")));
        when(accountTableMetaData.containsIndex("status_idx_exist")).thenReturn(true);
        when(accountTableMetaData.getPrimaryKeyColumns()).thenReturn(Collections.singletonList("account_id"));
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.getTable("t_account")).thenReturn(accountTableMetaData);
        when(result.getTable("t_account_detail")).thenReturn(mock(ShardingSphereTable.class));
        when(result.getAllColumnNames("t_account")).thenReturn(new ArrayList<>(Arrays.asList("account_id", "amount", "status")));
        when(result.getAllColumnNames("t_user")).thenReturn(new ArrayList<>(Arrays.asList("id", "content")));
        when(result.getAllColumnNames("t_user_extend")).thenReturn(new ArrayList<>(Arrays.asList("user_id", "content")));
        when(result.getVisibleColumnNames("t_account")).thenReturn(new ArrayList<>(Arrays.asList("account_id", "amount")));
        when(result.getVisibleColumnNames("t_user")).thenReturn(new ArrayList<>(Arrays.asList("id", "content")));
        when(result.getVisibleColumnNames("t_user_extend")).thenReturn(new ArrayList<>(Arrays.asList("user_id", "content")));
        when(result.containsColumn("t_account", "account_id")).thenReturn(true);
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.containsTable("t_account_detail")).thenReturn(true);
        when(result.containsTable("t_user")).thenReturn(true);
        when(result.containsTable("t_user_extend")).thenReturn(true);
        when(result.containsTable("t_single")).thenReturn(true);
        when(result.containsTable("t_single_extend")).thenReturn(true);
        when(result.containsTable("t_config")).thenReturn(true);
        when(result.containsTable("T_ROLE")).thenReturn(true);
        when(result.containsTable("T_ROLE_ADMIN")).thenReturn(true);
        when(result.containsTable("t_account_view")).thenReturn(true);
        return Collections.singletonMap(schemaName, result);
    }
    
    private Collection<ShardingSphereColumn> createColumnMetaDataMap() {
        Collection<ShardingSphereColumn> result = new LinkedList<>();
        result.add(new ShardingSphereColumn("account_id", Types.INTEGER, true, true, false, true, false));
        result.add(mock(ShardingSphereColumn.class));
        result.add(mock(ShardingSphereColumn.class));
        return result;
    }
    
    @Override
    protected void mockDataSource(final Map<String, DataSource> dataSources) {
    }
}
