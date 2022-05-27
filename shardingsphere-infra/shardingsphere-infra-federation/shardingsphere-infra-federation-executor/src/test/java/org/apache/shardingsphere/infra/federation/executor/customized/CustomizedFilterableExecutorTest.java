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

package org.apache.shardingsphere.infra.federation.executor.customized;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Before;
import org.junit.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomizedFilterableExecutorTest {
    
    private CustomizedFilterableExecutor executor;
    
    @Before
    public void init() throws Exception {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(2, 1);
        tableMetaDataMap.put("t_order_federate", createOrderTableMetaData());
        tableMetaDataMap.put("t_user_info", createUserInfoTableMetaData());
        String schemaName = "federate_jdbc";
        String databaseName = "database_name";
        Map<String, ShardingSphereSchema> schemas = Collections.singletonMap(databaseName, new ShardingSphereSchema(tableMetaDataMap));
        ShardingSphereDatabase metaData = new ShardingSphereDatabase(schemaName, new H2DatabaseType(), mockResource(), null, schemas);
        OptimizerContext optimizerContext = OptimizerContextFactory.create(Collections.singletonMap(schemaName, metaData), createGlobalRuleMetaData());
        executor = new CustomizedFilterableExecutor(databaseName, schemaName, optimizerContext);
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData() {
        CacheOption cacheOption = new CacheOption(128, 1024L);
        return new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(new SQLParserRule(new SQLParserRuleConfiguration(false, cacheOption, cacheOption))));
    }
    
    private ShardingSphereResource mockResource() {
        ShardingSphereResource result = mock(ShardingSphereResource.class);
        when(result.getDatabaseType()).thenReturn(new H2DatabaseType());
        return result;
    }
    
    private TableMetaData createOrderTableMetaData() {
        ColumnMetaData orderIdColumn = new ColumnMetaData("order_id", Types.VARCHAR, true, false, false);
        ColumnMetaData userIdColumn = new ColumnMetaData("user_id", Types.VARCHAR, false, false, false);
        ColumnMetaData statusColumn = new ColumnMetaData("status", Types.VARCHAR, false, false, false);
        return new TableMetaData("t_order_federate", Arrays.asList(orderIdColumn, userIdColumn, statusColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    private TableMetaData createUserInfoTableMetaData() {
        ColumnMetaData userIdColumn = new ColumnMetaData("user_id", Types.VARCHAR, true, false, false);
        ColumnMetaData informationColumn = new ColumnMetaData("information", Types.VARCHAR, false, false, false);
        return new TableMetaData("t_user_info", Arrays.asList(userIdColumn, informationColumn), Collections.emptyList(), Collections.emptyList());
    }
    
    @Test
    @SneakyThrows
    public void assertSelectWhereSingleField() {
        // TODO add executor.executeQuery()
    }
}
