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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import lombok.Getter;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCollationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

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
 * Character sets of MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_character_set.html">Character Set</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/charset-charsets.html">Character Set</a>
 */
@Getter
public enum MySQLCharacterSets {
    
    BIG5_CHINESE_CI(1, () -> Charset.forName("big5")),
    LATIN2_CZECH_CS(2, () -> Charset.forName("latin2")),
    DEC8_SWEDISH_CI(3, () -> Charset.forName("dec8")),
    CP850_GENERAL_CI(4, () -> Charset.forName("cp850")),
    LATIN1_GERMAN1_CI(5, () -> StandardCharsets.ISO_8859_1),
    HP8_ENGLISH_CI(6, () -> Charset.forName("hp8")),
    KOI8R_GENERAL_CI(7, () -> Charset.forName("koi8-r")),
    LATIN1_SWEDISH_CI(8, () -> StandardCharsets.ISO_8859_1),
    LATIN2_GENERAL_CI(9, () -> Charset.forName("latin2")),
    SWE7_SWEDISH_CI(10, () -> Charset.forName("swe7")),
    ASCII_GENERAL_CI(11, () -> StandardCharsets.US_ASCII),
    UJIS_JAPANESE_CI(12, () -> Charset.forName("ujis")),
    SJIS_JAPANESE_CI(13, () -> Charset.forName("sjis")),
    CP1251_BULGARIAN_CI(14, () -> Charset.forName("cp1251")),
    LATIN1_DANISH_CI(15, () -> StandardCharsets.ISO_8859_1),
    HEBREW_GENERAL_CI(16, () -> Charset.forName("hebrew")),
    TIS620_THAI_CI(18, () -> Charset.forName("tis620")),
    EUCKR_KOREAN_CI(19, () -> Charset.forName("euckr")),
    LATIN7_ESTONIAN_CS(20, () -> Charset.forName("iso-8859-13")),
    LATIN2_HUNGARIAN_CI(21, () -> Charset.forName("latin2")),
    KOI8U_GENERAL_CI(22, () -> Charset.forName("koi8-u")),
    CP1251_UKRAINIAN_CI(23, () -> Charset.forName("cp1251")),
    GB2312_CHINESE_CI(24, () -> Charset.forName("gb2312")),
    GREEK_GENERAL_CI(25, () -> Charset.forName("greek")),
    CP1250_GENERAL_CI(26, () -> Charset.forName("cp1250")),
    LATIN2_CROATIAN_CI(27, () -> Charset.forName("latin2")),
    GBK_CHINESE_CI(28, () -> Charset.forName("gbk")),
    CP1257_LITHUANIAN_CI(29, () -> Charset.forName("cp1257")),
    LATIN5_TURKISH_CI(30, () -> Charset.forName("latin5")),
    LATIN1_GERMAN2_CI(31, () -> StandardCharsets.ISO_8859_1),
    ARMSCII8_GENERAL_CI(32, () -> Charset.forName("armscii8")),
    UTF8_GENERAL_CI(33, () -> StandardCharsets.UTF_8),
    CP1250_CZECH_CS(34, () -> Charset.forName("cp1250")),
    UCS2_GENERAL_CI(35, () -> Charset.forName("ucs2")),
    CP866_GENERAL_CI(36, () -> Charset.forName("cp866")),
    KEYBCS2_GENERAL_CI(37, () -> Charset.forName("keybcs2")),
    MACCE_GENERAL_CI(38, () -> Charset.forName("macce")),
    MACROMAN_GENERAL_CI(39, () -> Charset.forName("macroman")),
    CP852_GENERAL_CI(40, () -> Charset.forName("cp852")),
    LATIN7_GENERAL_CI(41, () -> Charset.forName("iso-8859-13")),
    LATIN7_GENERAL_CS(42, () -> Charset.forName("iso-8859-13")),
    MACCE_BIN(43, () -> Charset.forName("macce")),
    CP1250_CROATIAN_CI(44, () -> Charset.forName("cp1250")),
    UTF8MB4_GENERAL_CI(45, () -> StandardCharsets.UTF_8),
    UTF8MB4_BIN(46, () -> StandardCharsets.UTF_8),
    LATIN1_BIN(47, () -> StandardCharsets.ISO_8859_1),
    LATIN1_GENERAL_CI(48, () -> StandardCharsets.ISO_8859_1),
    LATIN1_GENERAL_CS(49, () -> StandardCharsets.ISO_8859_1),
    CP1251_BIN(50, () -> Charset.forName("cp1251")),
    CP1251_GENERAL_CI(51, () -> Charset.forName("cp1251")),
    CP1251_GENERAL_CS(52, () -> Charset.forName("cp1251")),
    MACROMAN_BIN(53, () -> Charset.forName("macroman")),
    UTF16_GENERAL_CI(54, () -> StandardCharsets.UTF_16),
    UTF16_BIN(55, () -> StandardCharsets.UTF_16),
    UTF16LE_GENERAL_CI(56, () -> StandardCharsets.UTF_16LE),
    CP1256_GENERAL_CI(57, () -> Charset.forName("cp1256")),
    CP1257_BIN(58, () -> Charset.forName("cp1257")),
    CP1257_GENERAL_CI(59, () -> Charset.forName("cp1257")),
    UTF32_GENERAL_CI(60, () -> Charset.forName("utf32")),
    UTF32_BIN(61, () -> Charset.forName("utf32")),
    UTF16LE_BIN(62, () -> StandardCharsets.UTF_16LE),
    BINARY(63, () -> Charset.forName("binary")),
    ARMSCII8_BIN(64, () -> Charset.forName("armscii8")),
    ASCII_BIN(65, () -> StandardCharsets.US_ASCII),
    CP1250_BIN(66, () -> Charset.forName("cp1250")),
    CP1256_BIN(67, () -> Charset.forName("cp1256")),
    CP866_BIN(68, () -> Charset.forName("cp866")),
    DEC8_BIN(69, () -> Charset.forName("dec8")),
    GREEK_BIN(70, () -> Charset.forName("greek")),
    HEBREW_BIN(71, () -> Charset.forName("hebrew")),
    HP8_BIN(72, () -> Charset.forName("hp8")),
    KEYBCS2_BIN(73, () -> Charset.forName("keybcs2")),
    KOI8R_BIN(74, () -> Charset.forName("koi8-r")),
    KOI8U_BIN(75, () -> Charset.forName("koi8-u")),
    UTF8_TOLOWER_CI(76, () -> StandardCharsets.UTF_8),
    LATIN2_BIN(77, () -> Charset.forName("latin2")),
    LATIN5_BIN(78, () -> Charset.forName("latin5")),
    LATIN7_BIN(79, () -> Charset.forName("iso-8859-13")),
    CP850_BIN(80, () -> Charset.forName("cp850")),
    CP852_BIN(81, () -> Charset.forName("cp852")),
    SWE7_BIN(82, () -> Charset.forName("swe7")),
    UTF8_BIN(83, () -> StandardCharsets.UTF_8),
    BIG5_BIN(84, () -> Charset.forName("big5")),
    EUCKR_BIN(85, () -> Charset.forName("euckr")),
    GB2312_BIN(86, () -> Charset.forName("gb2312")),
    GBK_BIN(87, () -> Charset.forName("gbk")),
    SJIS_BIN(88, () -> Charset.forName("sjis")),
    TIS620_BIN(89, () -> Charset.forName("tis620")),
    UCS2_BIN(90, () -> Charset.forName("ucs2")),
    UJIS_BIN(91, () -> Charset.forName("ujis")),
    GEOSTD8_GENERAL_CI(92, () -> Charset.forName("geostd8")),
    GEOSTD8_BIN(93, () -> Charset.forName("geostd8")),
    LATIN1_SPANISH_CI(94, () -> StandardCharsets.ISO_8859_1),
    CP932_JAPANESE_CI(95, () -> Charset.forName("cp932")),
    CP932_BIN(96, () -> Charset.forName("cp932")),
    EUCJPMS_JAPANESE_CI(97, () -> Charset.forName("eucjpms")),
    EUCJPMS_BIN(98, () -> Charset.forName("eucjpms")),
    CP1250_POLISH_CI(99, () -> Charset.forName("cp1250")),
    UTF16_UNICODE_CI(101, () -> StandardCharsets.UTF_16),
    UTF16_ICELANDIC_CI(102, () -> StandardCharsets.UTF_16),
    UTF16_LATVIAN_CI(103, () -> StandardCharsets.UTF_16),
    UTF16_ROMANIAN_CI(104, () -> StandardCharsets.UTF_16),
    UTF16_SLOVENIAN_CI(105, () -> StandardCharsets.UTF_16),
    UTF16_POLISH_CI(106, () -> StandardCharsets.UTF_16),
    UTF16_ESTONIAN_CI(107, () -> StandardCharsets.UTF_16),
    UTF16_SPANISH_CI(108, () -> StandardCharsets.UTF_16),
    UTF16_SWEDISH_CI(109, () -> StandardCharsets.UTF_16),
    UTF16_TURKISH_CI(110, () -> StandardCharsets.UTF_16),
    UTF16_CZECH_CI(111, () -> StandardCharsets.UTF_16),
    UTF16_DANISH_CI(112, () -> StandardCharsets.UTF_16),
    UTF16_LITHUANIAN_CI(113, () -> StandardCharsets.UTF_16),
    UTF16_SLOVAK_CI(114, () -> StandardCharsets.UTF_16),
    UTF16_SPANISH2_CI(115, () -> StandardCharsets.UTF_16),
    UTF16_ROMAN_CI(116, () -> StandardCharsets.UTF_16),
    UTF16_PERSIAN_CI(117, () -> StandardCharsets.UTF_16),
    UTF16_ESPERANTO_CI(118, () -> StandardCharsets.UTF_16),
    UTF16_HUNGARIAN_CI(119, () -> StandardCharsets.UTF_16),
    UTF16_SINHALA_CI(120, () -> StandardCharsets.UTF_16),
    UTF16_GERMAN2_CI(121, () -> StandardCharsets.UTF_16),
    UTF16_CROATIAN_CI(122, () -> StandardCharsets.UTF_16),
    UTF16_UNICODE_520_CI(123, () -> StandardCharsets.UTF_16),
    UTF16_VIETNAMESE_CI(124, () -> StandardCharsets.UTF_16),
    UCS2_UNICODE_CI(128, () -> Charset.forName("ucs2")),
    UCS2_ICELANDIC_CI(129, () -> Charset.forName("ucs2")),
    UCS2_LATVIAN_CI(130, () -> Charset.forName("ucs2")),
    UCS2_ROMANIAN_CI(131, () -> Charset.forName("ucs2")),
    UCS2_SLOVENIAN_CI(132, () -> Charset.forName("ucs2")),
    UCS2_POLISH_CI(133, () -> Charset.forName("ucs2")),
    UCS2_ESTONIAN_CI(134, () -> Charset.forName("ucs2")),
    UCS2_SPANISH_CI(135, () -> Charset.forName("ucs2")),
    UCS2_SWEDISH_CI(136, () -> Charset.forName("ucs2")),
    UCS2_TURKISH_CI(137, () -> Charset.forName("ucs2")),
    UCS2_CZECH_CI(138, () -> Charset.forName("ucs2")),
    UCS2_DANISH_CI(139, () -> Charset.forName("ucs2")),
    UCS2_LITHUANIAN_CI(140, () -> Charset.forName("ucs2")),
    UCS2_SLOVAK_CI(141, () -> Charset.forName("ucs2")),
    UCS2_SPANISH2_CI(142, () -> Charset.forName("ucs2")),
    UCS2_ROMAN_CI(143, () -> Charset.forName("ucs2")),
    UCS2_PERSIAN_CI(144, () -> Charset.forName("ucs2")),
    UCS2_ESPERANTO_CI(145, () -> Charset.forName("ucs2")),
    UCS2_HUNGARIAN_CI(146, () -> Charset.forName("ucs2")),
    UCS2_SINHALA_CI(147, () -> Charset.forName("ucs2")),
    UCS2_GERMAN2_CI(148, () -> Charset.forName("ucs2")),
    UCS2_CROATIAN_CI(149, () -> Charset.forName("ucs2")),
    UCS2_UNICODE_520_CI(150, () -> Charset.forName("ucs2")),
    UCS2_VIETNAMESE_CI(151, () -> Charset.forName("ucs2")),
    UCS2_GENERAL_MYSQL500_CI(159, () -> Charset.forName("ucs2")),
    UTF32_UNICODE_CI(160, () -> Charset.forName("utf32")),
    UTF32_ICELANDIC_CI(161, () -> Charset.forName("utf32")),
    UTF32_LATVIAN_CI(162, () -> Charset.forName("utf32")),
    UTF32_ROMANIAN_CI(163, () -> Charset.forName("utf32")),
    UTF32_SLOVENIAN_CI(164, () -> Charset.forName("utf32")),
    UTF32_POLISH_CI(165, () -> Charset.forName("utf32")),
    UTF32_ESTONIAN_CI(166, () -> Charset.forName("utf32")),
    UTF32_SPANISH_CI(167, () -> Charset.forName("utf32")),
    UTF32_SWEDISH_CI(168, () -> Charset.forName("utf32")),
    UTF32_TURKISH_CI(169, () -> Charset.forName("utf32")),
    UTF32_CZECH_CI(170, () -> Charset.forName("utf32")),
    UTF32_DANISH_CI(171, () -> Charset.forName("utf32")),
    UTF32_LITHUANIAN_CI(172, () -> Charset.forName("utf32")),
    UTF32_SLOVAK_CI(173, () -> Charset.forName("utf32")),
    UTF32_SPANISH2_CI(174, () -> Charset.forName("utf32")),
    UTF32_ROMAN_CI(175, () -> Charset.forName("utf32")),
    UTF32_PERSIAN_CI(176, () -> Charset.forName("utf32")),
    UTF32_ESPERANTO_CI(177, () -> Charset.forName("utf32")),
    UTF32_HUNGARIAN_CI(178, () -> Charset.forName("utf32")),
    UTF32_SINHALA_CI(179, () -> Charset.forName("utf32")),
    UTF32_GERMAN2_CI(180, () -> Charset.forName("utf32")),
    UTF32_CROATIAN_CI(181, () -> Charset.forName("utf32")),
    UTF32_UNICODE_520_CI(182, () -> Charset.forName("utf32")),
    UTF32_VIETNAMESE_CI(183, () -> Charset.forName("utf32")),
    UTF8_UNICODE_CI(192, () -> StandardCharsets.UTF_8),
    UTF8_ICELANDIC_CI(193, () -> StandardCharsets.UTF_8),
    UTF8_LATVIAN_CI(194, () -> StandardCharsets.UTF_8),
    UTF8_ROMANIAN_CI(195, () -> StandardCharsets.UTF_8),
    UTF8_SLOVENIAN_CI(196, () -> StandardCharsets.UTF_8),
    UTF8_POLISH_CI(197, () -> StandardCharsets.UTF_8),
    UTF8_ESTONIAN_CI(198, () -> StandardCharsets.UTF_8),
    UTF8_SPANISH_CI(199, () -> StandardCharsets.UTF_8),
    UTF8_SWEDISH_CI(200, () -> StandardCharsets.UTF_8),
    UTF8_TURKISH_CI(201, () -> StandardCharsets.UTF_8),
    UTF8_CZECH_CI(202, () -> StandardCharsets.UTF_8),
    UTF8_DANISH_CI(203, () -> StandardCharsets.UTF_8),
    UTF8_LITHUANIAN_CI(204, () -> StandardCharsets.UTF_8),
    UTF8_SLOVAK_CI(205, () -> StandardCharsets.UTF_8),
    UTF8_SPANISH2_CI(206, () -> StandardCharsets.UTF_8),
    UTF8_ROMAN_CI(207, () -> StandardCharsets.UTF_8),
    UTF8_PERSIAN_CI(208, () -> StandardCharsets.UTF_8),
    UTF8_ESPERANTO_CI(209, () -> StandardCharsets.UTF_8),
    UTF8_HUNGARIAN_CI(210, () -> StandardCharsets.UTF_8),
    UTF8_SINHALA_CI(211, () -> StandardCharsets.UTF_8),
    UTF8_GERMAN2_CI(212, () -> StandardCharsets.UTF_8),
    UTF8_CROATIAN_CI(213, () -> StandardCharsets.UTF_8),
    UTF8_UNICODE_520_CI(214, () -> StandardCharsets.UTF_8),
    UTF8_VIETNAMESE_CI(215, () -> StandardCharsets.UTF_8),
    UTF8_GENERAL_MYSQL500_CI(223, () -> StandardCharsets.UTF_8),
    UTF8MB4_UNICODE_CI(224, () -> StandardCharsets.UTF_8),
    UTF8MB4_ICELANDIC_CI(225, () -> StandardCharsets.UTF_8),
    UTF8MB4_LATVIAN_CI(226, () -> StandardCharsets.UTF_8),
    UTF8MB4_ROMANIAN_CI(227, () -> StandardCharsets.UTF_8),
    UTF8MB4_SLOVENIAN_CI(228, () -> StandardCharsets.UTF_8),
    UTF8MB4_POLISH_CI(229, () -> StandardCharsets.UTF_8),
    UTF8MB4_ESTONIAN_CI(230, () -> StandardCharsets.UTF_8),
    UTF8MB4_SPANISH_CI(231, () -> StandardCharsets.UTF_8),
    UTF8MB4_SWEDISH_CI(232, () -> StandardCharsets.UTF_8),
    UTF8MB4_TURKISH_CI(233, () -> StandardCharsets.UTF_8),
    UTF8MB4_CZECH_CI(234, () -> StandardCharsets.UTF_8),
    UTF8MB4_DANISH_CI(235, () -> StandardCharsets.UTF_8),
    UTF8MB4_LITHUANIAN_CI(236, () -> StandardCharsets.UTF_8),
    UTF8MB4_SLOVAK_CI(237, () -> StandardCharsets.UTF_8),
    UTF8MB4_SPANISH2_CI(238, () -> StandardCharsets.UTF_8),
    UTF8MB4_ROMAN_CI(239, () -> StandardCharsets.UTF_8),
    UTF8MB4_PERSIAN_CI(240, () -> StandardCharsets.UTF_8),
    UTF8MB4_ESPERANTO_CI(241, () -> StandardCharsets.UTF_8),
    UTF8MB4_HUNGARIAN_CI(242, () -> StandardCharsets.UTF_8),
    UTF8MB4_SINHALA_CI(243, () -> StandardCharsets.UTF_8),
    UTF8MB4_GERMAN2_CI(244, () -> StandardCharsets.UTF_8),
    UTF8MB4_CROATIAN_CI(245, () -> StandardCharsets.UTF_8),
    UTF8MB4_UNICODE_520_CI(246, () -> StandardCharsets.UTF_8),
    UTF8MB4_VIETNAMESE_CI(247, () -> StandardCharsets.UTF_8),
    GB18030_CHINESE_CI(248, () -> Charset.forName("gb18030")),
    GB18030_BIN(249, () -> Charset.forName("gb18030")),
    GB18030_UNICODE_520_CI(250, () -> Charset.forName("gb18030")),
    UTF8MB4_0900_AI_CI(255, () -> StandardCharsets.UTF_8),
    UTF8MB4_DE_PB_0900_AI_CI(256, () -> StandardCharsets.UTF_8),
    UTF8MB4_IS_0900_AI_CI(257, () -> StandardCharsets.UTF_8),
    UTF8MB4_LV_0900_AI_CI(258, () -> StandardCharsets.UTF_8),
    UTF8MB4_RO_0900_AI_CI(259, () -> StandardCharsets.UTF_8),
    UTF8MB4_SL_0900_AI_CI(260, () -> StandardCharsets.UTF_8),
    UTF8MB4_PL_0900_AI_CI(261, () -> StandardCharsets.UTF_8),
    UTF8MB4_ET_0900_AI_CI(262, () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_0900_AI_CI(263, () -> StandardCharsets.UTF_8),
    UTF8MB4_SV_0900_AI_CI(264, () -> StandardCharsets.UTF_8),
    UTF8MB4_TR_0900_AI_CI(265, () -> StandardCharsets.UTF_8),
    UTF8MB4_CS_0900_AI_CI(266, () -> StandardCharsets.UTF_8),
    UTF8MB4_DA_0900_AI_CI(267, () -> StandardCharsets.UTF_8),
    UTF8MB4_LT_0900_AI_CI(268, () -> StandardCharsets.UTF_8),
    UTF8MB4_SK_0900_AI_CI(269, () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_TRAD_0900_AI_CI(270, () -> StandardCharsets.UTF_8),
    UTF8MB4_LA_0900_AI_CI(271, () -> StandardCharsets.UTF_8),
    UTF8MB4_EO_0900_AI_CI(273, () -> StandardCharsets.UTF_8),
    UTF8MB4_HU_0900_AI_CI(274, () -> StandardCharsets.UTF_8),
    UTF8MB4_HR_0900_AI_CI(275, () -> StandardCharsets.UTF_8),
    UTF8MB4_VI_0900_AI_CI(277, () -> StandardCharsets.UTF_8),
    UTF8MB4_0900_AS_CS(278, () -> StandardCharsets.UTF_8),
    UTF8MB4_DE_PB_0900_AS_CS(279, () -> StandardCharsets.UTF_8),
    UTF8MB4_IS_0900_AS_CS(280, () -> StandardCharsets.UTF_8),
    UTF8MB4_LV_0900_AS_CS(281, () -> StandardCharsets.UTF_8),
    UTF8MB4_RO_0900_AS_CS(282, () -> StandardCharsets.UTF_8),
    UTF8MB4_SL_0900_AS_CS(283, () -> StandardCharsets.UTF_8),
    UTF8MB4_PL_0900_AS_CS(284, () -> StandardCharsets.UTF_8),
    UTF8MB4_ET_0900_AS_CS(285, () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_0900_AS_CS(286, () -> StandardCharsets.UTF_8),
    UTF8MB4_SV_0900_AS_CS(287, () -> StandardCharsets.UTF_8),
    UTF8MB4_TR_0900_AS_CS(288, () -> StandardCharsets.UTF_8),
    UTF8MB4_CS_0900_AS_CS(289, () -> StandardCharsets.UTF_8),
    UTF8MB4_DA_0900_AS_CS(290, () -> StandardCharsets.UTF_8),
    UTF8MB4_LT_0900_AS_CS(291, () -> StandardCharsets.UTF_8),
    UTF8MB4_SK_0900_AS_CS(292, () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_TRAD_0900_AS_CS(293, () -> StandardCharsets.UTF_8),
    UTF8MB4_LA_0900_AS_CS(294, () -> StandardCharsets.UTF_8),
    UTF8MB4_EO_0900_AS_CS(296, () -> StandardCharsets.UTF_8),
    UTF8MB4_HU_0900_AS_CS(297, () -> StandardCharsets.UTF_8),
    UTF8MB4_HR_0900_AS_CS(298, () -> StandardCharsets.UTF_8),
    UTF8MB4_VI_0900_AS_CS(300, () -> StandardCharsets.UTF_8),
    UTF8MB4_JA_0900_AS_CS(303, () -> StandardCharsets.UTF_8),
    UTF8MB4_JA_0900_AS_CS_KS(304, () -> StandardCharsets.UTF_8),
    UTF8MB4_0900_AS_CI(305, () -> StandardCharsets.UTF_8),
    UTF8MB4_RU_0900_AI_CI(306, () -> StandardCharsets.UTF_8),
    UTF8MB4_RU_0900_AS_CS(307, () -> StandardCharsets.UTF_8),
    UTF8MB4_ZH_0900_AS_CS(308, () -> StandardCharsets.UTF_8),
    UTF8MB4_0900_BIN(309, () -> StandardCharsets.UTF_8);
    
    private static final Map<Integer, MySQLCharacterSets> CHARACTER_SET_MAP = Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(each -> each.id, Function.identity())));
    
    private final int id;
    
    private final Charset charset;
    
    MySQLCharacterSets(final int id, final Supplier<Charset> charsetSupplier) {
        this.id = id;
        Charset result = null;
        try {
            result = charsetSupplier.get();
        } catch (final UnsupportedCharsetException ignored) {
        }
        charset = result;
    }
    
    /**
     * Get character set by id.
     *
     * @param id id
     * @return MySQL character set
     */
    public static MySQLCharacterSets findById(final int id) {
        MySQLCharacterSets result = CHARACTER_SET_MAP.get(id);
        ShardingSpherePreconditions.checkNotNull(result, () -> new UnknownCollationException(id));
        ShardingSpherePreconditions.checkNotNull(result.getCharset(), () -> new UnknownCollationException(id));
        return result;
    }
}
