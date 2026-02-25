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

package org.apache.shardingsphere.database.connector.hive.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiveDatabaseMetaDataTest {
    
    private final DialectDatabaseMetaData dialectDatabaseMetaData = DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, TypedSPILoader.getService(DatabaseType.class, "Hive"));
    
    @Test
    void assertGetQuoteCharacter() {
        assertThat(dialectDatabaseMetaData.getQuoteCharacter(), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    void assertGetDefaultNullsOrderType() {
        assertThat(dialectDatabaseMetaData.getDefaultNullsOrderType(), is(NullsOrderType.LOW));
    }
    
    @Test
    void assertGetIdentifierPatternType() {
        assertThat(dialectDatabaseMetaData.getIdentifierPatternType(), is(IdentifierPatternType.KEEP_ORIGIN));
    }
    
    @Test
    void assertGetSchemaOption() {
        DialectSchemaOption actual = dialectDatabaseMetaData.getSchemaOption();
        assertThat(actual, isA(DefaultSchemaOption.class));
        assertFalse(actual.isSchemaAvailable());
        assertTrue(actual.getDefaultSchema().isPresent());
        assertThat(actual.getDefaultSchema().get(), is("default"));
        assertFalse(actual.getDefaultSystemSchema().isPresent());
    }
    
    @Test
    void assertGetSQLBatchOption() {
        assertFalse(dialectDatabaseMetaData.getSQLBatchOption().isSupportSQLBatch());
    }
}
