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

package org.apache.shardingsphere.core.metadata.datasource;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class ShardingSphereDataSourceMetaDataTest {
    
    private DataSourceMetas dataSourceMetas;
    
    @Before
    public void setUp() {
        Map<String, String> dataSourceURLs = new LinkedHashMap<>();
        dataSourceURLs.put("ds_0", "jdbc:mysql://127.0.0.1:3306/db_0");
        dataSourceURLs.put("ds_1", "jdbc:mysql://127.0.0.1:3306/db_1");
        dataSourceMetas = new DataSourceMetas(dataSourceURLs, DatabaseTypes.getActualDatabaseType("MySQL"));
    }
    
    @Test
    public void assertGetAllInstanceDataSourceNamesForShardingRule() {
        assertEquals(dataSourceMetas.getAllInstanceDataSourceNames(), Lists.newArrayList("ds_1"));
    }
    
    @Test
    public void assertGetActualSchemaNameForShardingRule() {
        assertEquals(dataSourceMetas.getDataSourceMetaData("ds_0").getSchemaName(), "db_0");
    }
}
