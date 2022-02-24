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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Character set of MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/character-set.html#packet-Protocol::CharacterSet">Character Set</a>
 */
@Getter
public enum MySQLCharacterSet {
    
    BIG5_CHINESE_CI(1, "big5", () -> Charset.forName("big5")),
    LATIN2_CZECH_CS(2, "latin2", () -> Charset.forName("latin2")),
    DEC8_SWEDISH_CI(3, "dec8", () -> Charset.forName("dec8")),
    CP850_GENERAL_CI(4, "cp850", () -> Charset.forName("cp850")),
    LATIN1_GERMAN1_CI(5, "latin1", () -> StandardCharsets.ISO_8859_1),
    HP8_ENGLISH_CI(6, "hp8", () -> Charset.forName("hp8")),
    KOI8R_GENERAL_CI(7, "koi8r", () -> Charset.forName("koi8r")),
    LATIN1_SWEDISH_CI(8, "latin1", () -> StandardCharsets.ISO_8859_1),
    LATIN2_GENERAL_CI(9, "latin2", () -> Charset.forName("latin2")),
    SWE7_SWEDISH_CI(10, "swe7", () -> Charset.forName("swe7")),
    ASCII_GENERAL_CI(11, "ascii", () -> StandardCharsets.US_ASCII),
    UJIS_JAPANESE_CI(12, "ujis", () -> Charset.forName("ujis")),
    SJIS_JAPANESE_CI(13, "sjis", () -> Charset.forName("sjis")),
    CP1251_BULGARIAN_CI(14, "cp1251", () -> Charset.forName("cp1251")),
    LATIN1_DANISH_CI(15, "latin1", () -> StandardCharsets.ISO_8859_1),
    HEBREW_GENERAL_CI(16, "hebrew", () -> Charset.forName("hebrew")),
    TIS620_THAI_CI(18, "tis620", () -> Charset.forName("tis620")),
    EUCKR_KOREAN_CI(19, "euckr", () -> Charset.forName("euckr")),
    LATIN7_ESTONIAN_CS(20, "latin7", () -> Charset.forName("latin7")),
    LATIN2_HUNGARIAN_CI(21, "latin2", () -> Charset.forName("latin2")),
    KOI8U_GENERAL_CI(22, "koi8u", () -> Charset.forName("koi8u")),
    CP1251_UKRAINIAN_CI(23, "cp1251", () -> Charset.forName("cp1251")),
    GB2312_CHINESE_CI(24, "gb2312", () -> Charset.forName("gb2312")),
    GREEK_GENERAL_CI(25, "greek", () -> Charset.forName("greek")),
    CP1250_GENERAL_CI(26, "cp1250", () -> Charset.forName("cp1250")),
    LATIN2_CROATIAN_CI(27, "latin2", () -> Charset.forName("latin2")),
    GBK_CHINESE_CI(28, "gbk", () -> Charset.forName("gbk")),
    CP1257_LITHUANIAN_CI(29, "cp1257", () -> Charset.forName("cp1257")),
    LATIN5_TURKISH_CI(30, "latin5", () -> Charset.forName("latin5")),
    LATIN1_GERMAN2_CI(31, "latin1", () -> StandardCharsets.ISO_8859_1),
    ARMSCII8_GENERAL_CI(32, "armscii8", () -> Charset.forName("armscii8")),
    UTF8_GENERAL_CI(33, "utf8", () -> StandardCharsets.UTF_8),
    CP1250_CZECH_CS(34, "cp1250", () -> Charset.forName("cp1250")),
    UCS2_GENERAL_CI(35, "ucs2", () -> Charset.forName("ucs2")),
    CP866_GENERAL_CI(36, "cp866", () -> Charset.forName("cp866")),
    KEYBCS2_GENERAL_CI(37, "keybcs2", () -> Charset.forName("keybcs2")),
    MACCE_GENERAL_CI(38, "macce", () -> Charset.forName("macce")),
    MACROMAN_GENERAL_CI(39, "macroman", () -> Charset.forName("macroman")),
    CP852_GENERAL_CI(40, "cp852", () -> Charset.forName("cp852")),
    LATIN7_GENERAL_CI(41, "latin7", () -> Charset.forName("latin7")),
    LATIN7_GENERAL_CS(42, "latin7", () -> Charset.forName("latin7")),
    MACCE_BIN(43, "macce", () -> Charset.forName("macce")),
    CP1250_CROATIAN_CI(44, "cp1250", () -> Charset.forName("cp1250")),
    UTF8MB4_GENERAL_CI(45, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_BIN(46, "utf8mb4", () -> StandardCharsets.UTF_8),
    LATIN1_BIN(47, "latin1", () -> StandardCharsets.ISO_8859_1),
    LATIN1_GENERAL_CI(48, "latin1", () -> StandardCharsets.ISO_8859_1),
    LATIN1_GENERAL_CS(49, "latin1", () -> StandardCharsets.ISO_8859_1),
    CP1251_BIN(50, "cp1251", () -> Charset.forName("cp1251")),
    CP1251_GENERAL_CI(51, "cp1251", () -> Charset.forName("cp1251")),
    CP1251_GENERAL_CS(52, "cp1251", () -> Charset.forName("cp1251")),
    MACROMAN_BIN(53, "macroman", () -> Charset.forName("macroman")),
    UTF16_GENERAL_CI(54, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_BIN(55, "utf16", () -> StandardCharsets.UTF_16),
    UTF16LE_GENERAL_CI(56, "utf16le", () -> StandardCharsets.UTF_16LE),
    CP1256_GENERAL_CI(57, "cp1256", () -> Charset.forName("cp1256")),
    CP1257_BIN(58, "cp1257", () -> Charset.forName("cp1257")),
    CP1257_GENERAL_CI(59, "cp1257", () -> Charset.forName("cp1257")),
    UTF32_GENERAL_CI(60, "utf32", () -> Charset.forName("utf32")),
    UTF32_BIN(61, "utf32", () -> Charset.forName("utf32")),
    UTF16LE_BIN(62, "utf16le", () -> StandardCharsets.UTF_16LE),
    BINARY(63, "binary", () -> Charset.forName("binary")),
    ARMSCII8_BIN(64, "armscii8", () -> Charset.forName("armscii8")),
    ASCII_BIN(65, "ascii", () -> StandardCharsets.US_ASCII),
    CP1250_BIN(66, "cp1250", () -> Charset.forName("cp1250")),
    CP1256_BIN(67, "cp1256", () -> Charset.forName("cp1256")),
    CP866_BIN(68, "cp866", () -> Charset.forName("cp866")),
    DEC8_BIN(69, "dec8", () -> Charset.forName("dec8")),
    GREEK_BIN(70, "greek", () -> Charset.forName("greek")),
    HEBREW_BIN(71, "hebrew", () -> Charset.forName("hebrew")),
    HP8_BIN(72, "hp8", () -> Charset.forName("hp8")),
    KEYBCS2_BIN(73, "keybcs2", () -> Charset.forName("keybcs2")),
    KOI8R_BIN(74, "koi8r", () -> Charset.forName("koi8r")),
    KOI8U_BIN(75, "koi8u", () -> Charset.forName("koi8u")),
    UTF8_TOLOWER_CI(76, "utf8", () -> StandardCharsets.UTF_8),
    LATIN2_BIN(77, "latin2", () -> Charset.forName("latin2")),
    LATIN5_BIN(78, "latin5", () -> Charset.forName("latin5")),
    LATIN7_BIN(79, "latin7", () -> Charset.forName("latin7")),
    CP850_BIN(80, "cp850", () -> Charset.forName("cp850")),
    CP852_BIN(81, "cp852", () -> Charset.forName("cp852")),
    SWE7_BIN(82, "swe7", () -> Charset.forName("swe7")),
    UTF8_BIN(83, "utf8", () -> StandardCharsets.UTF_8),
    BIG5_BIN(84, "big5", () -> Charset.forName("big5")),
    EUCKR_BIN(85, "euckr", () -> Charset.forName("euckr")),
    GB2312_BIN(86, "gb2312", () -> Charset.forName("gb2312")),
    GBK_BIN(87, "gbk", () -> Charset.forName("gbk")),
    SJIS_BIN(88, "sjis", () -> Charset.forName("sjis")),
    TIS620_BIN(89, "tis620", () -> Charset.forName("tis620")),
    UCS2_BIN(90, "ucs2", () -> Charset.forName("ucs2")),
    UJIS_BIN(91, "ujis", () -> Charset.forName("ujis")),
    GEOSTD8_GENERAL_CI(92, "geostd8", () -> Charset.forName("geostd8")),
    GEOSTD8_BIN(93, "geostd8", () -> Charset.forName("geostd8")),
    LATIN1_SPANISH_CI(94, "latin1", () -> StandardCharsets.ISO_8859_1),
    CP932_JAPANESE_CI(95, "cp932", () -> Charset.forName("cp932")),
    CP932_BIN(96, "cp932", () -> Charset.forName("cp932")),
    EUCJPMS_JAPANESE_CI(97, "eucjpms", () -> Charset.forName("eucjpms")),
    EUCJPMS_BIN(98, "eucjpms", () -> Charset.forName("eucjpms")),
    CP1250_POLISH_CI(99, "cp1250", () -> Charset.forName("cp1250")),
    UTF16_UNICODE_CI(101, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_ICELANDIC_CI(102, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_LATVIAN_CI(103, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_ROMANIAN_CI(104, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_SLOVENIAN_CI(105, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_POLISH_CI(106, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_ESTONIAN_CI(107, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_SPANISH_CI(108, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_SWEDISH_CI(109, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_TURKISH_CI(110, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_CZECH_CI(111, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_DANISH_CI(112, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_LITHUANIAN_CI(113, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_SLOVAK_CI(114, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_SPANISH2_CI(115, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_ROMAN_CI(116, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_PERSIAN_CI(117, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_ESPERANTO_CI(118, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_HUNGARIAN_CI(119, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_SINHALA_CI(120, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_GERMAN2_CI(121, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_CROATIAN_CI(122, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_UNICODE_520_CI(123, "utf16", () -> StandardCharsets.UTF_16),
    UTF16_VIETNAMESE_CI(124, "utf16", () -> StandardCharsets.UTF_16),
    UCS2_UNICODE_CI(128, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_ICELANDIC_CI(129, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_LATVIAN_CI(130, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_ROMANIAN_CI(131, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_SLOVENIAN_CI(132, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_POLISH_CI(133, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_ESTONIAN_CI(134, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_SPANISH_CI(135, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_SWEDISH_CI(136, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_TURKISH_CI(137, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_CZECH_CI(138, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_DANISH_CI(139, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_LITHUANIAN_CI(140, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_SLOVAK_CI(141, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_SPANISH2_CI(142, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_ROMAN_CI(143, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_PERSIAN_CI(144, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_ESPERANTO_CI(145, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_HUNGARIAN_CI(146, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_SINHALA_CI(147, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_GERMAN2_CI(148, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_CROATIAN_CI(149, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_UNICODE_520_CI(150, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_VIETNAMESE_CI(151, "ucs2", () -> Charset.forName("ucs2")),
    UCS2_GENERAL_MYSQL500_CI(159, "ucs2", () -> Charset.forName("ucs2")),
    UTF32_UNICODE_CI(160, "utf32", () -> Charset.forName("utf32")),
    UTF32_ICELANDIC_CI(161, "utf32", () -> Charset.forName("utf32")),
    UTF32_LATVIAN_CI(162, "utf32", () -> Charset.forName("utf32")),
    UTF32_ROMANIAN_CI(163, "utf32", () -> Charset.forName("utf32")),
    UTF32_SLOVENIAN_CI(164, "utf32", () -> Charset.forName("utf32")),
    UTF32_POLISH_CI(165, "utf32", () -> Charset.forName("utf32")),
    UTF32_ESTONIAN_CI(166, "utf32", () -> Charset.forName("utf32")),
    UTF32_SPANISH_CI(167, "utf32", () -> Charset.forName("utf32")),
    UTF32_SWEDISH_CI(168, "utf32", () -> Charset.forName("utf32")),
    UTF32_TURKISH_CI(169, "utf32", () -> Charset.forName("utf32")),
    UTF32_CZECH_CI(170, "utf32", () -> Charset.forName("utf32")),
    UTF32_DANISH_CI(171, "utf32", () -> Charset.forName("utf32")),
    UTF32_LITHUANIAN_CI(172, "utf32", () -> Charset.forName("utf32")),
    UTF32_SLOVAK_CI(173, "utf32", () -> Charset.forName("utf32")),
    UTF32_SPANISH2_CI(174, "utf32", () -> Charset.forName("utf32")),
    UTF32_ROMAN_CI(175, "utf32", () -> Charset.forName("utf32")),
    UTF32_PERSIAN_CI(176, "utf32", () -> Charset.forName("utf32")),
    UTF32_ESPERANTO_CI(177, "utf32", () -> Charset.forName("utf32")),
    UTF32_HUNGARIAN_CI(178, "utf32", () -> Charset.forName("utf32")),
    UTF32_SINHALA_CI(179, "utf32", () -> Charset.forName("utf32")),
    UTF32_GERMAN2_CI(180, "utf32", () -> Charset.forName("utf32")),
    UTF32_CROATIAN_CI(181, "utf32", () -> Charset.forName("utf32")),
    UTF32_UNICODE_520_CI(182, "utf32", () -> Charset.forName("utf32")),
    UTF32_VIETNAMESE_CI(183, "utf32", () -> Charset.forName("utf32")),
    UTF8_ICELANDIC_CI(193, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_LATVIAN_CI(194, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_ROMANIAN_CI(195, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_SLOVENIAN_CI(196, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_POLISH_CI(197, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_ESTONIAN_CI(198, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_SPANISH_CI(199, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_SWEDISH_CI(200, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_TURKISH_CI(201, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_CZECH_CI(202, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_DANISH_CI(203, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_LITHUANIAN_CI(204, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_SLOVAK_CI(205, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_SPANISH2_CI(206, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_ROMAN_CI(207, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_PERSIAN_CI(208, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_ESPERANTO_CI(209, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_HUNGARIAN_CI(210, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_SINHALA_CI(211, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_GERMAN2_CI(212, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_CROATIAN_CI(213, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_UNICODE_520_CI(214, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_VIETNAMESE_CI(215, "utf8", () -> StandardCharsets.UTF_8),
    UTF8_GENERAL_MYSQL500_CI(223, "utf8", () -> StandardCharsets.UTF_8),
    UTF8MB4_UNICODE_CI(224, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ICELANDIC_CI(225, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LATVIAN_CI(226, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ROMANIAN_CI(227, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SLOVENIAN_CI(228, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_POLISH_CI(229, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ESTONIAN_CI(230, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SPANISH_CI(231, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SWEDISH_CI(232, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_TURKISH_CI(233, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_CZECH_CI(234, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_DANISH_CI(235, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LITHUANIAN_CI(236, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SLOVAK_CI(237, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SPANISH2_CI(238, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ROMAN_CI(239, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_PERSIAN_CI(240, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ESPERANTO_CI(241, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_HUNGARIAN_CI(242, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SINHALA_CI(243, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_GERMAN2_CI(244, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_CROATIAN_CI(245, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_UNICODE_520_CI(246, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_VIETNAMESE_CI(247, "utf8mb4", () -> StandardCharsets.UTF_8),
    GB18030_CHINESE_CI(248, "gb18030", () -> Charset.forName("gb18030")),
    GB18030_BIN(249, "gb18030", () -> Charset.forName("gb18030")),
    GB18030_UNICODE_520_CI(250, "gb18030", () -> Charset.forName("gb18030")),
    UTF8MB4_0900_AI_CI(255, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_DE_PB_0900_AI_CI(256, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_IS_0900_AI_CI(257, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LV_0900_AI_CI(258, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_RO_0900_AI_CI(259, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SL_0900_AI_CI(260, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_PL_0900_AI_CI(261, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ET_0900_AI_CI(262, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_0900_AI_CI(263, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SV_0900_AI_CI(264, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_TR_0900_AI_CI(265, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_CS_0900_AI_CI(266, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_DA_0900_AI_CI(267, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LT_0900_AI_CI(268, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SK_0900_AI_CI(269, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_TRAD_0900_AI_CI(270, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LA_0900_AI_CI(271, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_EO_0900_AI_CI(273, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_HU_0900_AI_CI(274, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_HR_0900_AI_CI(275, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_VI_0900_AI_CI(277, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_0900_AS_CS(278, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_DE_PB_0900_AS_CS(279, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_IS_0900_AS_CS(280, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LV_0900_AS_CS(281, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_RO_0900_AS_CS(282, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SL_0900_AS_CS(283, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_PL_0900_AS_CS(284, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ET_0900_AS_CS(285, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_0900_AS_CS(286, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SV_0900_AS_CS(287, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_TR_0900_AS_CS(288, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_CS_0900_AS_CS(289, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_DA_0900_AS_CS(290, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LT_0900_AS_CS(291, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_SK_0900_AS_CS(292, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ES_TRAD_0900_AS_CS(293, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_LA_0900_AS_CS(294, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_EO_0900_AS_CS(296, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_HU_0900_AS_CS(297, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_HR_0900_AS_CS(298, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_VI_0900_AS_CS(300, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_JA_0900_AS_CS(303, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_JA_0900_AS_CS_KS(304, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_0900_AS_CI(305, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_RU_0900_AI_CI(306, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_RU_0900_AS_CS(307, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_ZH_0900_AS_CS(308, "utf8mb4", () -> StandardCharsets.UTF_8),
    UTF8MB4_0900_BIN(309, "utf8mb4", () -> StandardCharsets.UTF_8);
    
    private static final Map<Integer, MySQLCharacterSet> CHARACTER_ID_SET_MAP;
    
    private static final Map<String, MySQLCharacterSet> CHARACTER_NAME_SET_MAP;
    
    static {
        MySQLCharacterSet[] values = values();
        int length = values().length;
        Map<Integer, MySQLCharacterSet> idMap = new HashMap<>(length);
        Map<String, MySQLCharacterSet> nameMap = new HashMap<>(length);
        for (int i = 0; i < length; i++) {
            if (idMap.put(values[i].id, values[i]) != null) {
                throw new IllegalStateException("Duplicate key");
            }
            nameMap.put(values[i].charsetName, values[i]);
        }
        CHARACTER_ID_SET_MAP = Collections.unmodifiableMap(idMap);
        CHARACTER_NAME_SET_MAP = Collections.unmodifiableMap(nameMap);
    }
    
    private final int id;
    
    private final String charsetName;
    
    private final Charset charset;
    
    MySQLCharacterSet(final int id, final String charsetName, final Supplier<Charset> charsetSupplier) {
        this.id = id;
        this.charsetName = charsetName;
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
        MySQLCharacterSet result = CHARACTER_ID_SET_MAP.get(id);
        if (null == result) {
            throw new UnsupportedCharsetException(String.format("Character set corresponding to id %d not found", id));
        }
        if (null == result.getCharset()) {
            throw new UnsupportedCharsetException(String.format("Character set %s unsupported", result.name().toLowerCase()));
        }
        return result;
    }
    
    /**
     * Get character set by charsetName.
     *
     * @param charsetName charsetName
     * @return MySQL character set
     */
    public static Optional<Charset> findByValue(final String charsetName) {
        MySQLCharacterSet result = CHARACTER_NAME_SET_MAP.get(charsetName.toLowerCase(Locale.ROOT));
        return null == result || null == result.getCharset() ? Optional.empty() : Optional.of(result.getCharset());
    }
}
