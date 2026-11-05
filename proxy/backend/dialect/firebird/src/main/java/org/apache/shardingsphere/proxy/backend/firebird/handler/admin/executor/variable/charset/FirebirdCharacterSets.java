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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Character sets defined in Firebird.
 */
public enum FirebirdCharacterSets {
    
    ASCII(2, () -> StandardCharsets.US_ASCII, "USASCII", "ASCII7"),
    BIG_5(56, () -> Charset.forName("BIG5"), "BIG5", "DOS_950", "WIN_950"),
    DOS437(10, () -> Charset.forName("IBM437"), "DOS_437"),
    DOS737(9, () -> Charset.forName("IBM737"), "DOS_737"),
    DOS775(15, () -> Charset.forName("IBM775"), "DOS_775"),
    DOS850(11, () -> Charset.forName("IBM850"), "DOS_850"),
    DOS852(45, () -> Charset.forName("IBM852"), "DOS_852"),
    DOS857(46, () -> Charset.forName("IBM857"), "DOS_857"),
    DOS858(16, () -> Charset.forName("IBM858"), "DOS_858"),
    DOS860(13, () -> Charset.forName("IBM860"), "DOS_860"),
    DOS861(47, () -> Charset.forName("IBM861"), "DOS_861"),
    DOS862(17, () -> Charset.forName("IBM862"), "DOS_862"),
    DOS863(14, () -> Charset.forName("IBM863"), "DOS_863"),
    DOS864(18, () -> Charset.forName("IBM864"), "DOS_864"),
    DOS865(12, () -> Charset.forName("IBM865"), "DOS_865"),
    DOS866(48, () -> Charset.forName("IBM866"), "DOS_866"),
    DOS869(49, () -> Charset.forName("IBM869"), "DOS_869"),
    EUCJ_0208(6, () -> Charset.forName("EUC-JP"), "EUCJ"),
    GB_2312(57, () -> Charset.forName("GB2312"), "GB2312", "DOS_936", "WIN_936"),
    ISO8859_1(21, () -> StandardCharsets.ISO_8859_1, "ISO88591", "LATIN1", "ANSI"),
    ISO8859_2(22, () -> Charset.forName("ISO8859_2"), "ISO88592", "LATIN2", "ISO-8859-2"),
    ISO8859_3(23, () -> Charset.forName("ISO8859_3"), "ISO88593", "LATIN3", "ISO-8859-3"),
    ISO8859_4(34, () -> Charset.forName("ISO8859_4"), "ISO88594", "LATIN4", "ISO-8859-4"),
    ISO8859_5(35, () -> Charset.forName("ISO8859_5"), "ISO88595", "ISO-8859-5"),
    ISO8859_6(36, () -> Charset.forName("ISO8859_6"), "ISO88596", "ISO-8859-6"),
    ISO8859_7(37, () -> Charset.forName("ISO8859_7"), "ISO88597", "ISO-8859-7"),
    ISO8859_8(38, () -> Charset.forName("ISO8859_8"), "ISO88598", "ISO-8859-8"),
    ISO8859_9(39, () -> Charset.forName("ISO8859_9"), "ISO88599", "LATIN5", "ISO-8859-9"),
    ISO8859_13(40, () -> Charset.forName("ISO8859_13"), "ISO885913", "LATIN7", "ISO-8859-13"),
    KOI8R(63, () -> Charset.forName("KOI8-R")),
    KOI8U(64, () -> Charset.forName("KOI8-U")),
    KSC_5601(44, () -> Charset.forName("EUC-KR"), "KSC5601", "DOS_949", "WIN_949"),
    
    // default jvm value as described here: https://www.firebirdsql.org/file/documentation/drivers_documentation/java/faq.html#how-can-i-specify-the-connection-character-set
    NONE(0, Charset::defaultCharset),
    OCTETS(1, Charset::defaultCharset, "BINARY"),
    
    SJIS_0208(5, () -> FirebirdSjisCharset.INSTANCE),
    UNICODE_FSS(3, () -> StandardCharsets.UTF_8, "UTF_FSS", "SQL_TEXT"),
    UTF8(4, () -> StandardCharsets.UTF_8, "UTF-8"),
    WIN1250(51, () -> Charset.forName("windows-1250"), "WIN_1250"),
    WIN1251(52, () -> Charset.forName("windows-1251"), "WIN_1251"),
    WIN1252(53, () -> Charset.forName("windows-1252"), "WIN_1252"),
    WIN1253(54, () -> Charset.forName("windows-1253"), "WIN_1253"),
    WIN1254(55, () -> Charset.forName("windows-1254"), "WIN_1254"),
    WIN1255(58, () -> Charset.forName("windows-1255"), "WIN_1255"),
    WIN1256(59, () -> Charset.forName("windows-1256"), "WIN_1256"),
    WIN1257(60, () -> Charset.forName("windows-1257"), "WIN_1257"),
    WIN1258(65, () -> Charset.forName("windows-1258"), "WIN_1258");
    
    private static final Map<String, FirebirdCharacterSets> CHARACTER_SETS_MAP;
    
    private static final Map<String, Integer> CHARACTER_SET_IDS = new HashMap<>(128, 1F);
    
    static {
        Map<String, FirebirdCharacterSets> map = new HashMap<>(64, 1F);
        for (FirebirdCharacterSets each : values()) {
            map.put(each.name(), each);
            CHARACTER_SET_IDS.put(normalizeCharacterSetName(each.name()), each.id);
            for (String eachAlias : each.aliases) {
                map.put(eachAlias.toUpperCase(), each);
                CHARACTER_SET_IDS.put(normalizeCharacterSetName(eachAlias), each.id);
            }
        }
        CHARACTER_SETS_MAP = map;
        CHARACTER_SET_IDS.put("USASCII", ASCII.id);
        CHARACTER_SET_IDS.put("ASCII7", ASCII.id);
        CHARACTER_SET_IDS.put("UTFFSS", UNICODE_FSS.id);
        CHARACTER_SET_IDS.put("SQLTEXT", UNICODE_FSS.id);
        CHARACTER_SET_IDS.put("SJIS", SJIS_0208.id);
        CHARACTER_SET_IDS.put("EUCJ", EUCJ_0208.id);
        CHARACTER_SET_IDS.put("LATIN1", ISO8859_1.id);
        CHARACTER_SET_IDS.put("ANSI", ISO8859_1.id);
        CHARACTER_SET_IDS.put("LATIN2", ISO8859_2.id);
        CHARACTER_SET_IDS.put("LATIN3", ISO8859_3.id);
        CHARACTER_SET_IDS.put("LATIN4", ISO8859_4.id);
        CHARACTER_SET_IDS.put("LATIN5", ISO8859_9.id);
        CHARACTER_SET_IDS.put("LATIN7", ISO8859_13.id);
        CHARACTER_SET_IDS.put("DOS949", KSC_5601.id);
        CHARACTER_SET_IDS.put("WIN949", KSC_5601.id);
        CHARACTER_SET_IDS.put("DOS950", BIG_5.id);
        CHARACTER_SET_IDS.put("WIN950", BIG_5.id);
        CHARACTER_SET_IDS.put("DOS936", GB_2312.id);
        CHARACTER_SET_IDS.put("WIN936", GB_2312.id);
        CHARACTER_SET_IDS.put("TIS620", 66);
        CHARACTER_SET_IDS.put("GBK", 67);
        CHARACTER_SET_IDS.put("CP943C", 68);
        CHARACTER_SET_IDS.put("GB18030", 69);
    }
    
    private final int id;
    
    private final Charset charset;
    
    private final String[] aliases;
    
    FirebirdCharacterSets(final int id, final Supplier<Charset> charsetSupplier, final String... aliases) {
        Charset result = null;
        try {
            result = charsetSupplier.get();
        } catch (final UnsupportedCharsetException ignored) {
        }
        this.id = id;
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
        FirebirdCharacterSets result = CHARACTER_SETS_MAP.get(formattedCharsetName.toUpperCase(Locale.ROOT));
        return null == result || null == result.charset ? Charset.forName(formattedCharsetName) : result.charset;
    }
    
    private static String formatValue(final String value) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(value) || QuoteCharacter.QUOTE.isWrapped(value) ? value.substring(1, value.length() - 1) : value.trim();
    }
    
    /**
     * Find the Firebird attachment character set identifier without inferring it from the Java charset.
     *
     * @param charsetName character set name or alias defined in Firebird
     * @return Firebird character set identifier
     * @throws UnsupportedCharsetException if the name has no Firebird character set identifier
     */
    public static int findCharacterSetId(final String charsetName) {
        Integer result = CHARACTER_SET_IDS.get(normalizeCharacterSetName(formatValue(charsetName)));
        ShardingSpherePreconditions.checkNotNull(result, () -> new UnsupportedCharsetException(charsetName));
        return result;
    }
    
    private static String normalizeCharacterSetName(final String charsetName) {
        return charsetName.toUpperCase(Locale.ENGLISH).replace("_", "").replace("-", "");
    }
}
