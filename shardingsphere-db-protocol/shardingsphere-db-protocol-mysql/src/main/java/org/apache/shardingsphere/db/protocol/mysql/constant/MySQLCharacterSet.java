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

package org.apache.shardingsphere.db.protocol.mysql.constant;

import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Character set of MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/character-set.html#packet-Protocol::CharacterSet">Character Set</a>
 */
@Getter
public enum MySQLCharacterSet {
    
    BIG5(1, () -> Charset.forName("big5")),
    DEC8(3, () -> Charset.forName("dec8")),
    CP850(4, () -> Charset.forName("cp850")),
    HP8(6, () -> Charset.forName("hp8")),
    KOI8R(7, () -> Charset.forName("koi8-u")),
    LATIN1(8, () -> StandardCharsets.ISO_8859_1),
    LATIN2(9, () -> Charset.forName("latin2")),
    SWE7(10, () -> Charset.forName("swe7")),
    ASCII(11, () -> StandardCharsets.US_ASCII),
    UJIS(12, () -> Charset.forName("ujis")),
    SJIS(13, () -> Charset.forName("sjis")),
    HEBREW(16, () -> Charset.forName("hebrew")),
    TIS620(18, () -> Charset.forName("tis620")),
    EUCKR(19, () -> Charset.forName("euckr")),
    KOI8U(22, () -> Charset.forName("koi8-u")),
    GB2312(24, () -> Charset.forName("gb2312")),
    GREEK(25, () -> Charset.forName("greek")),
    CP1250(26, () -> Charset.forName("cp1250")),
    GBK(28, () -> Charset.forName("gbk")),
    LATIN5(30, () -> Charset.forName("latin5")),
    ARMSCII8(32, () -> Charset.forName("armscii8")),
    UTF8(33, () -> StandardCharsets.UTF_8),
    UCS2(35, () -> Charset.forName("ucs2")),
    CP866(36, () -> Charset.forName("cp866")),
    KEYBCS2(37, () -> Charset.forName("keybcs2")),
    MACCE(38, () -> Charset.forName("macce")),
    MACROMAN(39, () -> Charset.forName("macroman")),
    CP852(40, () -> Charset.forName("cp852")),
    LATIN7(41, () -> Charset.forName("iso-8859-13")),
    CP1251(51, () -> Charset.forName("cp1251")),
    UTF16(54, () -> StandardCharsets.UTF_16),
    UTF16LE(56, () -> StandardCharsets.UTF_16LE),
    CP1256(57, () -> Charset.forName("cp1256")),
    CP1257(59, () -> Charset.forName("cp1257")),
    UTF32(60, () -> Charset.forName("utf32")),
    BINARY(63, () -> null),
    GEOSTD8(92, () -> Charset.forName("geostd8")),
    CP932(95, () -> Charset.forName("cp932")),
    EUCJPMS(97, () -> Charset.forName("eucjpms")),
    GB18030(248, () -> Charset.forName("gb18030")),
    UTF8MB4(255, () -> StandardCharsets.UTF_8);
    
    private static final Map<Integer, MySQLCharacterSet> CHARACTER_SET_MAP = Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(each -> each.id, Function.identity())));
    
    private final int id;
    
    private final Charset charset;
    
    MySQLCharacterSet(final int id, final Supplier<Charset> charsetSupplier) {
        this.id = id;
        Charset result = null;
        try {
            result = charsetSupplier.get();
        } catch (UnsupportedCharsetException ignored) {
        }
        charset = result;
    }
    
    /**
     * Get character set by id.
     *
     * @param id id
     * @return MySQL character set
     */
    public static MySQLCharacterSet findById(final int id) {
        MySQLCharacterSet result = CHARACTER_SET_MAP.get(id);
        if (null == result) {
            throw new UnsupportedCharsetException(String.format("Character set corresponding to id %d not found", id));
        }
        if (null == result.getCharset()) {
            throw new UnsupportedCharsetException(String.format("Character set %s unsupported", result.name().toLowerCase()));
        }
        return result;
    }
}
