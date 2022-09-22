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

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.exception.syntax.UnsupportedTranslatedDatabaseException;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class SQLTranslatorRuleTest {
    
    @Test
    public void assertTranslateWhenProtocolSameAsStorage() {
        String expected = "select 1";
        PostgreSQLDatabaseType databaseType = new PostgreSQLDatabaseType();
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).translate(expected, null, databaseType, databaseType);
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertTranslateWhenNoStorage() {
        String expected = "select 1";
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).translate(expected, null, new PostgreSQLDatabaseType(), null);
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertTranslateWithProtocolDifferentWithStorage() {
        String input = "select 1";
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).translate(input, null, new PostgreSQLDatabaseType(), new MySQLDatabaseType());
        assertThat(actual, is(input.toUpperCase(Locale.ROOT)));
    }
    
    @Test
    public void assertUseOriginalSQLWhenTranslatingFailed() {
        String expected = "select 1";
        String actual = new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("ALWAYS_FAILED", true)).translate(expected, null, new PostgreSQLDatabaseType(), new MySQLDatabaseType());
        assertThat(actual, is(expected));
    }
    
    @Test(expected = UnsupportedTranslatedDatabaseException.class)
    public void assertNotUseOriginalSQLWhenTranslatingFailed() {
        new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("ALWAYS_FAILED", false)).translate("", null, new PostgreSQLDatabaseType(), new MySQLDatabaseType());
    }
    
    @Test
    public void assertGetConfiguration() {
        SQLTranslatorRuleConfiguration expected = new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false);
        assertThat(new SQLTranslatorRule(expected).getConfiguration(), is(expected));
    }
    
    @Test
    public void assertGetType() {
        assertThat(new SQLTranslatorRule(new SQLTranslatorRuleConfiguration("CONVERT_TO_UPPER_CASE", false)).getType(), is(SQLTranslatorRule.class.getSimpleName()));
    }
}
