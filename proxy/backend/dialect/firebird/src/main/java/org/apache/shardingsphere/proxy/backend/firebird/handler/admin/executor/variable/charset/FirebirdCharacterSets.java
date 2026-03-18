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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Character sets defined in Firebird.
 */
public enum FirebirdCharacterSets {
    
    ASCII(() -> StandardCharsets.US_ASCII),
    BIG_5(() -> Charset.forName("BIG5")),
    // CYRL(() -> Charset.forName("CYRL")),
    DOS437(() -> Charset.forName("IBM437")),
    DOS737(() -> Charset.forName("IBM737")),
    DOS775(() -> Charset.forName("IBM775")),
    DOS850(() -> Charset.forName("IBM850")),
    DOS852(() -> Charset.forName("IBM852")),
    DOS857(() -> Charset.forName("IBM857")),
    DOS858(() -> Charset.forName("IBM858")),
    DOS860(() -> Charset.forName("IBM860")),
    DOS861(() -> Charset.forName("IBM861")),
    DOS862(() -> Charset.forName("IBM862")),
    DOS863(() -> Charset.forName("IBM863")),
    DOS864(() -> Charset.forName("IBM864")),
    DOS865(() -> Charset.forName("IBM865")),
    DOS866(() -> Charset.forName("IBM866")),
    DOS869(() -> Charset.forName("IBM869")),
    EUCJ_0208(() -> Charset.forName("EUC-JP")),
    GB_2312(() -> Charset.forName("GB2312")),
    ISO8859_1(() -> StandardCharsets.ISO_8859_1),
    ISO8859_2(() -> Charset.forName("ISO8859_2")),
    ISO8859_3(() -> Charset.forName("ISO8859_3")),
    ISO8859_4(() -> Charset.forName("ISO8859_4")),
    ISO8859_5(() -> Charset.forName("ISO8859_5")),
    ISO8859_6(() -> Charset.forName("ISO8859_6")),
    ISO8859_7(() -> Charset.forName("ISO8859_7")),
    ISO8859_8(() -> Charset.forName("ISO8859_8")),
    ISO8859_9(() -> Charset.forName("ISO8859_9")),
    ISO8859_13(() -> Charset.forName("ISO8859_13")),
    KOI8R(() -> Charset.forName("KOI8-R")),
    KOI8U(() -> Charset.forName("KOI8-U")),
    KSC_5601(() -> Charset.forName("EUC-KR")),
    // NEXT(() -> Charset.forName("NEXT")),
    
    // default jvm value as described here: https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#how-can-i-specify-the-connection-character-set
    NONE(Charset::defaultCharset),
    OCTETS(Charset::defaultCharset, "BINARY"),
    
    SJIS_0208(() -> Charset.forName("ISO-2022-JP")),
    UNICODE_FSS(() -> StandardCharsets.UTF_8),
    UTF8(() -> StandardCharsets.UTF_8),
    WIN1250(() -> Charset.forName("windows-1250")),
    WIN1251(() -> Charset.forName("windows-1251")),
    WIN1252(() -> Charset.forName("windows-1252")),
    WIN1253(() -> Charset.forName("windows-1253")),
    WIN1254(() -> Charset.forName("windows-1254")),
    WIN1255(() -> Charset.forName("windows-1255")),
    WIN1256(() -> Charset.forName("windows-1256")),
    WIN1257(() -> Charset.forName("windows-1257")),
    WIN1258(() -> Charset.forName("windows-1258"));
    
    private static final Map<String, FirebirdCharacterSets> CHARACTER_SETS_MAP;
    
    static {
        Map<String, FirebirdCharacterSets> map = new HashMap<>(64, 1F);
        for (FirebirdCharacterSets each : values()) {
            map.put(each.name(), each);
            for (String eachAlias : each.aliases) {
                map.put(eachAlias.toUpperCase(), each);
            }
        }
        CHARACTER_SETS_MAP = map;
    }
    
    private final Charset charset;
    
    private final String[] aliases;
    
    FirebirdCharacterSets(final Supplier<Charset> charsetSupplier, final String... aliases) {
        Charset result = null;
        try {
            result = charsetSupplier.get();
        } catch (final UnsupportedCharsetException ignored) {
        }
        charset = result;
        this.aliases = aliases;
    }
    
    /**
     * Find corresponding {@link Charset} by charset name defined in Firebird.
     *
     * @param charsetName charset name defined in Firebird
     * @return corresponding {@link Charset}
     */
    public static Charset findCharacterSet(final String charsetName) {
        String formattedCharsetName = formatValue(charsetName);
        FirebirdCharacterSets result = CHARACTER_SETS_MAP.get(formattedCharsetName.toUpperCase());
        return null == result || null == result.charset ? Charset.forName(formattedCharsetName) : result.charset;
    }
    
    private static String formatValue(final String value) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(value) || QuoteCharacter.QUOTE.isWrapped(value) ? value.substring(1, value.length() - 1) : value.trim();
    }
}
