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

package org.apache.shardingsphere.infra.database.type.dialect;

import org.apache.shardingsphere.infra.database.metadata.dialect.SQLServerDataSourceMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SQLServerDatabaseTypeTest {
    
    @Test
    public void assertGetQuoteCharacter() {
        assertThat(new SQLServerDatabaseType().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
    }
    
    @Test
    public void assertGetJdbcUrlPrefixes() {
        assertThat(new SQLServerDatabaseType().getJdbcUrlPrefixes(), is(Arrays.asList("jdbc:microsoft:sqlserver:", "jdbc:sqlserver:")));
    }
    
    @Test
    public void assertGetDataSourceMetaData() {
        assertThat(new SQLServerDatabaseType().getDataSourceMetaData("jdbc:sqlserver://127.0.0.1;DatabaseName=ds_0", "root"), instanceOf(SQLServerDataSourceMetaData.class));
        assertThat(new SQLServerDatabaseType().getDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=ds_0", "sa"), instanceOf(SQLServerDataSourceMetaData.class));
    }
    
    @Test
    public void assertGetSystemDatabases() {
        assertTrue(new SQLServerDatabaseType().getSystemDatabaseSchemaMap().isEmpty());
    }
    
    @Test
    public void assertGetSystemSchemas() {
        assertTrue(new SQLServerDatabaseType().getSystemSchemas().isEmpty());
    }
}
