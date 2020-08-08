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

package org.apache.shardingsphere.infra.metadata.datasource;

import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public final class DataSourceMetasTest {

    @Test
    public void assertGetAllInstanceDataSourceNamesForShardingRuleByDifferentDataSource() {
        Map<String, DatabaseAccessConfiguration> databaseAccessConfigurationMap = new HashMap<>(2, 1);
        databaseAccessConfigurationMap.put("ds_0", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_0", "test", null));
        databaseAccessConfigurationMap.put("ds_1", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3307/db_1", "test", null));
        DataSourceMetas dataSourceMetas = new DataSourceMetas(DatabaseTypes.getActualDatabaseType("MySQL"), databaseAccessConfigurationMap);
        Collection<String> allInstanceDataSourceNames = dataSourceMetas.getAllInstanceDataSourceNames();
        assertNotNull(allInstanceDataSourceNames);
        assertThat(allInstanceDataSourceNames.size(), is(2));
        assertTrue(allInstanceDataSourceNames.contains("ds_0") && allInstanceDataSourceNames.contains("ds_1"));
    }

    @Test
    public void assertGetAllInstanceDataSourceNamesForShardingRuleBySameDataSource() {
        Map<String, DatabaseAccessConfiguration> databaseAccessConfigurationMap = new HashMap<>(2, 1);
        databaseAccessConfigurationMap.put("ds_0", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_0", "test", null));
        databaseAccessConfigurationMap.put("ds_1", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_1", "test", null));
        DataSourceMetas dataSourceMetas = new DataSourceMetas(DatabaseTypes.getActualDatabaseType("MySQL"), databaseAccessConfigurationMap);
        Collection<String> allInstanceDataSourceNames = dataSourceMetas.getAllInstanceDataSourceNames();
        assertNotNull(allInstanceDataSourceNames);
        assertThat(allInstanceDataSourceNames.size(), is(1));
        assertTrue(allInstanceDataSourceNames.contains("ds_0") || allInstanceDataSourceNames.contains("ds_1"));
    }

    @Test
    public void assertGetActualCatalogForShardingRule() {
        Map<String, DatabaseAccessConfiguration> databaseAccessConfigurationMap = new HashMap<>(2, 1);
        databaseAccessConfigurationMap.put("ds_0", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_0", "test", null));
        databaseAccessConfigurationMap.put("ds_1", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_1", "test", null));
        DataSourceMetas dataSourceMetas = new DataSourceMetas(DatabaseTypes.getActualDatabaseType("MySQL"), databaseAccessConfigurationMap);
        assertThat(dataSourceMetas.getDataSourceMetaData("ds_0").getCatalog(), is("db_0"));
    }

    @Test
    public void assertGetActualSchemaNameForShardingRuleForMysql() {
        Map<String, DatabaseAccessConfiguration> databaseAccessConfigurationMap = new HashMap<>(2, 1);
        databaseAccessConfigurationMap.put("ds_0", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_0", "test", null));
        databaseAccessConfigurationMap.put("ds_1", new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/db_1", "test", null));
        DataSourceMetas dataSourceMetas = new DataSourceMetas(DatabaseTypes.getActualDatabaseType("MySQL"), databaseAccessConfigurationMap);
        assertNull(dataSourceMetas.getDataSourceMetaData("ds_0").getSchema());
    }
}
