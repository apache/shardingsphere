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

package org.apache.shardingsphere.sqltranslator.rule;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.exception.UnsupportedTranslatedDatabaseException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLTranslatorRuleTest {
    
    @Test
    void assertTranslateWhenProtocolSameAsStorage() {
        String expected = "SELECT 1";
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).thenReturn(databaseType);
        Optional<SQLTranslatorContext> actual = createSQLTranslatorRule(false).translate(expected, Collections.emptyList(), queryContext, databaseType, mock(), mock());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertTranslateWhenNoStorage() {
        String expected = "SELECT 1";
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).thenReturn(sqlParserType);
        Optional<SQLTranslatorContext> actual = createSQLTranslatorRule(false).translate(expected, Collections.emptyList(), queryContext, null, mock(), mock());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertTranslateWithProtocolDifferentWithStorage() {
        String input = "SELECT 1";
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).thenReturn(sqlParserType);
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Optional<SQLTranslatorContext> actual = createSQLTranslatorRule(false).translate(input, Collections.emptyList(), queryContext, storageType, mock(), mock());
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSql(), is(input.toUpperCase(Locale.ROOT)));
    }
    
    @Test
    void assertUseOriginalSQLWhenTranslatingFailed() {
        String expected = "ERROR: SELECT 1";
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).thenReturn(sqlParserType);
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        Optional<SQLTranslatorContext> actual = createSQLTranslatorRule(true).translate(expected, Collections.emptyList(), queryContext, storageType, mock(), mock());
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertNotUseOriginalSQLWhenTranslatingFailed() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when(queryContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).thenReturn(sqlParserType);
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        assertThrows(UnsupportedTranslatedDatabaseException.class,
                () -> createSQLTranslatorRule(false).translate("ERROR: SELECT 1", Collections.emptyList(), queryContext, storageType, mock(), mock()));
    }
    
    @Test
    void assertGetConfiguration() {
        SQLTranslatorRuleConfiguration expected = new SQLTranslatorRuleConfiguration("CORE:FIXTURE", new Properties(), false);
        assertThat(new SQLTranslatorRule(expected).getConfiguration(), is(expected));
    }
    
    private SQLTranslatorRule createSQLTranslatorRule(final boolean useOriginalSQLWhenTranslatingFailed) {
        return new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CORE:FIXTURE", new Properties(), useOriginalSQLWhenTranslatingFailed));
    }
}
