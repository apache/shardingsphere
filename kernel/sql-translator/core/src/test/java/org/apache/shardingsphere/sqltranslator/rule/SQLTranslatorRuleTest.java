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

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.exception.syntax.UnsupportedTranslatedDatabaseException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLTranslatorRuleTest {
    
    @Test
    void assertTranslateWhenProtocolSameAsStorage() {
        String expected = "select 1";
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getDatabaseType()).thenReturn(databaseType);
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).translate(expected, queryContext, databaseType, mock(ShardingSphereDatabase.class),
                mock(RuleMetaData.class));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertTranslateWhenNoStorage() {
        String expected = "select 1";
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getDatabaseType()).thenReturn(sqlParserType);
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).translate(expected, queryContext, null, mock(ShardingSphereDatabase.class),
                mock(RuleMetaData.class));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertTranslateWithProtocolDifferentWithStorage() {
        String input = "select 1";
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getDatabaseType()).thenReturn(sqlParserType);
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).translate(input, queryContext, storageType, mock(ShardingSphereDatabase.class),
                mock(RuleMetaData.class));
        assertThat(actual, is(input.toUpperCase(Locale.ROOT)));
    }
    
    @Test
    void assertUseOriginalSQLWhenTranslatingFailed() {
        String expected = "select 1";
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext().getDatabaseType()).thenReturn(sqlParserType);
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("ALWAYS_FAILED", true)).translate(expected, queryContext, storageType, mock(ShardingSphereDatabase.class),
                mock(RuleMetaData.class));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertNotUseOriginalSQLWhenTranslatingFailed() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        DatabaseType sqlParserType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        when(queryContext.getSqlStatementContext().getDatabaseType()).thenReturn(sqlParserType);
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        assertThrows(UnsupportedTranslatedDatabaseException.class, () -> new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("ALWAYS_FAILED", false)).translate("", queryContext, storageType,
                mock(ShardingSphereDatabase.class), mock(RuleMetaData.class)));
    }
    
    @Test
    void assertGetConfiguration() {
        SQLTranslatorRuleConfiguration expected = new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false);
        assertThat(new SQLTranslatorRule(expected).getConfiguration(), is(expected));
    }
}
