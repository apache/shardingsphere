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

package org.apache.shardingsphere.infra.binder.postgresql;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLIdentifierUtilsTest {
    
    @Test
    void assertFoldAsciiLetters() {
        assertThat(PostgreSQLIdentifierUtils.fold("AbS_1"), is("abs_1"));
    }
    
    @Test
    void assertFoldLeavesNonAsciiLettersUnchanged() {
        assertThat(PostgreSQLIdentifierUtils.fold("ÄBC"), is("Äbc"));
    }
    
    @Test
    void assertFoldWithTurkishDefaultLocale() {
        Locale originalLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try {
            assertThat(PostgreSQLIdentifierUtils.fold("INITCAP"), is("initcap"));
        } finally {
            Locale.setDefault(originalLocale);
        }
    }
    
    @Test
    void assertIsUnicodeQuoted() {
        assertTrue(PostgreSQLIdentifierUtils.isUnicodeQuoted("U&\"MyFunc\""));
        assertTrue(PostgreSQLIdentifierUtils.isUnicodeQuoted("u&\"MyFunc\""));
        assertTrue(PostgreSQLIdentifierUtils.isUnicodeQuoted("U&\"\""));
    }
    
    @Test
    void assertIsNotUnicodeQuoted() {
        assertFalse(PostgreSQLIdentifierUtils.isUnicodeQuoted("ABS"));
        assertFalse(PostgreSQLIdentifierUtils.isUnicodeQuoted("\"MyFunc\""));
        assertFalse(PostgreSQLIdentifierUtils.isUnicodeQuoted("U&\""));
        assertFalse(PostgreSQLIdentifierUtils.isUnicodeQuoted("U?\"abc\""));
        assertFalse(PostgreSQLIdentifierUtils.isUnicodeQuoted("U&'abc'"));
        assertFalse(PostgreSQLIdentifierUtils.isUnicodeQuoted("U&\"abc"));
    }
    
    @Test
    void assertUnquoteUnicodeWithoutEscape() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"MyFunc\""), is("MyFunc"));
    }
    
    @Test
    void assertUnquoteUnicodeWithShortAndLongEscape() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"d\\0061t\\+000041\""), is("datA"));
    }
    
    @Test
    void assertUnquoteUnicodeWithSurrogatePairEscape() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"\\D83D\\DE00\""), is("😀"));
    }
    
    @Test
    void assertUnquoteUnicodeWithEscapedEscapeCharacter() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"a\\\\b\""), is("a\\b"));
    }
    
    @Test
    void assertUnquoteUnicodeWithUescapeClause() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("u&\"d!0061t\"UESCAPE'!'"), is("dat"));
    }
    
    @Test
    void assertUnquoteUnicodeWithSpacedAndLowerCaseUescapeClause() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"d!0061t\" uescape '!' "), is("dat"));
    }
    
    @Test
    void assertUnquoteUnicodeWithMalformedUescapeClauseFallsBackToDefaultEscape() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"d\\0061t\"UESCAPE'!!'"), is("dat"));
    }
    
    @Test
    void assertUnquoteUnicodeWithNonHexEscapeDigits() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"a\\zz12b\""), is("a\\zz12b"));
    }
    
    @Test
    void assertUnquoteUnicodeWithTruncatedEscape() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"ab\\00\""), is("ab\\00"));
    }
    
    @Test
    void assertUnquoteUnicodeWithTrailingEscapeCharacter() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"ab\\\""), is("ab\\"));
    }
    
    @Test
    void assertUnquoteUnicodeWithOutOfRangeCodePoint() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"a\\+FFFFFFb\""), is("a\\+FFFFFFb"));
    }
    
    @Test
    void assertUnquoteUnicodeWithEmptyContent() {
        assertThat(PostgreSQLIdentifierUtils.unquoteUnicode("U&\"\""), is(""));
    }
}
