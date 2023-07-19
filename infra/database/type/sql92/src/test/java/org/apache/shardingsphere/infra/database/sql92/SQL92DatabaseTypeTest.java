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

package org.apache.shardingsphere.infra.database.sql92;

import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQL92DatabaseTypeTest {
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(new SQL92DatabaseType().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetJdbcUrlPrefixes() {
        assertThat(new SQL92DatabaseType().getJdbcUrlPrefixes(), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetDataSourceMetaData() {
        assertThat(new SQL92DatabaseType().getDataSourceMetaData("jdbc:xxx", "root"), instanceOf(SQL92DataSourceMetaData.class));
    }
    
    @Test
    void assertGetSystemDatabases() {
        assertTrue(new SQL92DatabaseType().getSystemDatabaseSchemaMap().isEmpty());
    }
    
    @Test
    void assertGetSystemSchemas() {
        assertTrue(new SQL92DatabaseType().getSystemSchemas().isEmpty());
    }
}
