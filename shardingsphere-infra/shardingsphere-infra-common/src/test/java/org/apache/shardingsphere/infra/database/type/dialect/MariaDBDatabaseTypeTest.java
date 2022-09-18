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

import org.apache.shardingsphere.infra.database.metadata.dialect.MariaDBDataSourceMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MariaDBDatabaseTypeTest {
    
    @Test
    public void assertGetQuoteCharacter() {
        assertThat(new MariaDBDatabaseType().getQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    public void assertGetJdbcUrlPrefixes() {
        assertThat(new MariaDBDatabaseType().getJdbcUrlPrefixes(), is(Collections.singleton("jdbc:mariadb:")));
    }
    
    @Test
    public void assertGetDataSourceMetaData() {
        assertThat(new MariaDBDatabaseType().getDataSourceMetaData("jdbc:mariadb://localhost:3306/foo_ds", "root"), instanceOf(MariaDBDataSourceMetaData.class));
    }
    
    @Test
    public void assertGetTrunkDatabaseType() {
        assertThat(new MariaDBDatabaseType().getTrunkDatabaseType().getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetSystemDatabaseSchemaMap() {
        assertTrue(new MariaDBDatabaseType().getSystemDatabaseSchemaMap().isEmpty());
    }
    
    @Test
    public void assertGetSystemSchemas() {
        assertTrue(new MariaDBDatabaseType().getSystemSchemas().isEmpty());
    }
}
