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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.variable.charset;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Character sets defined in PostgreSQL.
 * <a href="https://www.postgresql.org/docs/14/multibyte.html">24.3. Character Set Support</a>
 */
public enum PostgreSQLCharacterSets {
    
    SQL_ASCII(() -> StandardCharsets.US_ASCII),
    EUC_JP(() -> Charset.forName("EUC_JP")),
    EUC_CN(() -> Charset.forName("EUC_CN")),
    EUC_KR(() -> Charset.forName("EUC_KR")),
    EUC_TW(() -> Charset.forName("EUC_TW")),
    EUC_JIS_2004(() -> Charset.forName("EUC_JIS_2004")),
    UTF8(() -> StandardCharsets.UTF_8, "Unicode", "UTF_8"),
    MULE_INTERNAL(() -> Charset.forName("MULE_INTERNAL")),
    LATIN1(() -> StandardCharsets.ISO_8859_1, "ISO88591"),
    LATIN2(() -> Charset.forName("LATIN2"), "ISO88592"),
    LATIN3(() -> Charset.forName("LATIN3"), "ISO88593"),
    LATIN4(() -> Charset.forName("LATIN4"), "ISO88594"),
    LATIN5(() -> Charset.forName("LATIN5"), "ISO88599"),
    LATIN6(() -> Charset.forName("ISO-8859-10"), "ISO885910"),
    LATIN7(() -> Charset.forName("ISO-8859-13"), "ISO885913"),
    LATIN8(() -> Charset.forName("ISO-8859-14"), "ISO885914"),
    LATIN9(() -> Charset.forName("LATIN9"), "ISO885915"),
    LATIN10(() -> Charset.forName("LATIN10"), "ISO885916"),
    WIN1256(() -> Charset.forName("WINDOWS-1256")),
    WIN1258(() -> Charset.forName("WINDOWS-1258"), "ABC", "TCVN", "TCVN5712", "VSCII"),
    WIN866(() -> Charset.forName("WINDOWS-866"), "ALT"),
    WIN874(() -> Charset.forName("WINDOWS-874")),
    KOI8R(() -> Charset.forName("KOI8-R")),
    WIN1251(() -> Charset.forName("WINDOWS-1251"), "WIN"),
    WIN1252(() -> Charset.forName("WINDOWS-1252")),
    ISO_8859_5(() -> Charset.forName("ISO-8859-5")),
    ISO_8859_6(() -> Charset.forName("ISO-8859-6")),
    ISO_8859_7(() -> Charset.forName("ISO-8859-7")),
    ISO_8859_8(() -> Charset.forName("ISO-8859-8")),
    WIN1250(() -> Charset.forName("WINDOWS-1250")),
    WIN1253(() -> Charset.forName("WINDOWS-1253")),
    WIN1254(() -> Charset.forName("WINDOWS-1254")),
    WIN1255(() -> Charset.forName("WINDOWS-1255")),
    WIN1257(() -> Charset.forName("WINDOWS-1257")),
    KOI8U(() -> Charset.forName("KOI8-U"), "KOI8"),
    SJIS(() -> Charset.forName("SJIS"), "Mskanji", "ShiftJIS", "WIN932", "Windows932"),
    BIG5(() -> Charset.forName("BIG5"), "WIN950", "Windows950"),
    GBK(() -> Charset.forName("GBK"), "WIN936", "Windows936"),
    UHC(() -> Charset.forName("UHC"), "WIN949", "Windows949"),
    GB18030(() -> Charset.forName("GB18030")),
    JOHAB(() -> Charset.forName("JOHAB")),
    SHIFT_JIS_2004(() -> Charset.forName("SHIFT_JIS"));
    
    private static final Map<String, PostgreSQLCharacterSets> CHARACTER_SETS_MAP;
    
    static {
        Map<String, PostgreSQLCharacterSets> map = new HashMap<>(128, 1F);
        for (PostgreSQLCharacterSets each : values()) {
            map.put(each.name(), each);
            for (String eachAlias : each.aliases) {
                map.put(eachAlias.toUpperCase(), each);
            }
        }
        CHARACTER_SETS_MAP = map;
    }
    
    private final Charset charset;
    
    private final String[] aliases;
    
    PostgreSQLCharacterSets(final Supplier<Charset> charsetSupplier, final String... aliases) {
        Charset result = null;
        try {
            result = charsetSupplier.get();
        } catch (final UnsupportedCharsetException ignored) {
        }
        charset = result;
        this.aliases = aliases;
    }
    
    /**
     * Find corresponding {@link Charset} by charset name defined in PostgreSQL.
     *
     * @param charsetName charset name defined in PostgreSQL
     * @return corresponding {@link Charset}
     */
    public static Charset findCharacterSet(final String charsetName) {
        String formattedCharsetName = formatValue(charsetName);
        PostgreSQLCharacterSets result = CHARACTER_SETS_MAP.get(formattedCharsetName.toUpperCase());
        return null == result || null == result.charset ? Charset.forName(formattedCharsetName) : result.charset;
    }
    
    private static String formatValue(final String value) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(value) || QuoteCharacter.QUOTE.isWrapped(value) ? value.substring(1, value.length() - 1) : value.trim();
    }
}
