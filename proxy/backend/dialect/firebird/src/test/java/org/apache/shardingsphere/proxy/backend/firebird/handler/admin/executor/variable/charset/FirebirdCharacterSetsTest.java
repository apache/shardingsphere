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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdCharacterSetsTest {
    
    @ParameterizedTest(name = "{0} has Firebird charset id {1}")
    @CsvSource({"NONE,0", "ASCII,2", "UNICODE_FSS,3", "UTF8,4", "WIN1251,52", "utf8,4", "UTF-8,4", "LATIN1,21", "WIN_1251,52"})
    void assertFindCharacterSetId(final String charsetName, final int expected) {
        assertThat(FirebirdCharacterSets.findCharacterSetId(charsetName), is(expected));
    }
    
    @Test
    void assertFindCharacterSetIdWithUnknownName() {
        assertThrows(UnsupportedCharsetException.class, () -> FirebirdCharacterSets.findCharacterSetId("foo_charset"));
    }
    
    @Test
    void assertFindSqlAscii() {
        assertThat(FirebirdCharacterSets.findCharacterSet("ASCII"), is(StandardCharsets.US_ASCII));
    }
    
    @Test
    void assertFindUTF8() {
        assertThat(FirebirdCharacterSets.findCharacterSet("utf8"), is(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertFindSjis() {
        assertThat(FirebirdCharacterSets.findCharacterSet("SJIS_0208"), sameInstance(FirebirdSjisCharset.INSTANCE));
    }
    
    @Test
    void assertFindUTF8WithQuotes() {
        assertThat(FirebirdCharacterSets.findCharacterSet("'utf8'"), is(StandardCharsets.UTF_8));
    }
    
    @ParameterizedTest(name = "{0} is an alias of {1}")
    @CsvSource({"USASCII,ASCII", "usascii,ASCII", "ASCII7,ASCII", "UTF_FSS,UNICODE_FSS", "SQL_TEXT,UNICODE_FSS", "UTF-8,UTF8", "EUCJ,EUCJ_0208",
            "DOS_437,DOS437", "DOS_737,DOS737", "DOS_775,DOS775", "DOS_850,DOS850", "DOS_852,DOS852", "DOS_857,DOS857", "DOS_858,DOS858",
            "DOS_860,DOS860", "DOS_861,DOS861", "DOS_862,DOS862", "DOS_863,DOS863", "DOS_864,DOS864", "DOS_865,DOS865", "DOS_866,DOS866", "DOS_869,DOS869",
            "ISO88591,ISO8859_1", "LATIN1,ISO8859_1", "ANSI,ISO8859_1", "ISO88592,ISO8859_2", "LATIN2,ISO8859_2", "ISO-8859-2,ISO8859_2",
            "ISO88593,ISO8859_3", "LATIN3,ISO8859_3", "ISO-8859-3,ISO8859_3", "ISO88594,ISO8859_4", "LATIN4,ISO8859_4", "ISO-8859-4,ISO8859_4",
            "ISO88595,ISO8859_5", "ISO-8859-5,ISO8859_5", "ISO88596,ISO8859_6", "ISO-8859-6,ISO8859_6", "ISO88597,ISO8859_7", "ISO-8859-7,ISO8859_7",
            "ISO88598,ISO8859_8", "ISO-8859-8,ISO8859_8", "ISO88599,ISO8859_9", "LATIN5,ISO8859_9", "ISO-8859-9,ISO8859_9",
            "ISO885913,ISO8859_13", "LATIN7,ISO8859_13", "ISO-8859-13,ISO8859_13", "WIN_1250,WIN1250", "WIN_1251,WIN1251", "win_1251,WIN1251", "WIN_1252,WIN1252",
            "WIN_1253,WIN1253", "WIN_1254,WIN1254", "WIN_1255,WIN1255", "WIN_1256,WIN1256", "WIN_1257,WIN1257", "WIN_1258,WIN1258",
            "KSC5601,KSC_5601", "DOS_949,KSC_5601", "WIN_949,KSC_5601", "BIG5,BIG_5", "DOS_950,BIG_5", "WIN_950,BIG_5", "GB2312,GB_2312",
            "DOS_936,GB_2312", "WIN_936,GB_2312"})
    void assertFindFirebirdAlias(final String alias, final String canonicalName) {
        assertThat(FirebirdCharacterSets.findCharacterSet(alias), is(FirebirdCharacterSets.findCharacterSet(canonicalName)));
    }
    
    @Test
    void assertFindUnsupportedCharset() {
        assertThrows(UnsupportedCharsetException.class, () -> FirebirdCharacterSets.findCharacterSet("unknown_charset"));
    }
}
