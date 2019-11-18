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

package org.apache.shardingsphere.spi.database;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.apache.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import org.junit.Before;
import org.junit.Test;

public final class PostgreSQLDatabaseTypeTest {
    private DataSourceInfo dataSourceInfo;
    
    @Before
    public void setUp() {
        dataSourceInfo = new DataSourceInfo();
        dataSourceInfo.setUsername("test");
    }
    
    @Test
    public void assertDataSourceInfoParam() {
        dataSourceInfo.setUrl("jdbc:postgresql://127.0.0.1:9999/ds_0");
        PostgreSQLDatabaseType databaseType = new PostgreSQLDatabaseType();
        
        PostgreSQLDataSourceMetaData actual = (PostgreSQLDataSourceMetaData) databaseType.getDataSourceMetaData(dataSourceInfo);
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(9999));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertEquals(actual.getSchemaName(), null);
    }
}
