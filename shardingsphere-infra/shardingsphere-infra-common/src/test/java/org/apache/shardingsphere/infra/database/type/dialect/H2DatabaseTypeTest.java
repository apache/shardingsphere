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

import org.apache.shardingsphere.infra.database.metadata.dialect.H2DataSourceMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class H2DatabaseTypeTest {
    
    @Test
    public void assertGetQuoteCharacter() {
        assertThat(new H2DatabaseType().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    public void assertGetJdbcUrlPrefixes() {
        assertThat(new H2DatabaseType().getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:h2:")));
    }
    
    @Test
    public void assertGetDataSourceMetaData() {
        assertThat(new H2DatabaseType().getDataSourceMetaData("jdbc:h2:~:foo_ds", "sa"), instanceOf(H2DataSourceMetaData.class));
        assertThat(new H2DatabaseType().getDataSourceMetaData("jdbc:h2:mem:foo_ds", "sa"), instanceOf(H2DataSourceMetaData.class));
    }
    
    @Test
    public void assertGetTrunkDatabaseType() {
        assertThat(new H2DatabaseType().getTrunkDatabaseType().getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetSystemDatabaseSchemaMap() {
        assertTrue(new H2DatabaseType().getSystemDatabaseSchemaMap().isEmpty());
    }
    
    @Test
    public void assertGetSystemSchemas() {
        assertTrue(new H2DatabaseType().getSystemSchemas().isEmpty());
    }
}
