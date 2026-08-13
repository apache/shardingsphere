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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PostgreSQL identifier utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLIdentifierUtils {
    
    private static final Pattern UESCAPE_CLAUSE_PATTERN = Pattern.compile("\\s*UESCAPE\\s*'(.)'\\s*", Pattern.CASE_INSENSITIVE);
    
    private static final char DEFAULT_UNICODE_ESCAPE_CHARACTER = '\\';
    
    private static final int SHORT_UNICODE_ESCAPE_LENGTH = 4;
    
    private static final int LONG_UNICODE_ESCAPE_LENGTH = 6;
    
    /**
     * Fold unquoted identifier to lower case as PostgreSQL does.
     *
     * <p>PostgreSQL folds ASCII {@code A}-{@code Z} only and leaves other characters unchanged,
     * so JVM default locale rules must not be applied here.</p>
     *
     * @param identifier identifier to be folded
     * @return folded identifier
     */
    public static String fold(final String identifier) {
        char[] result = identifier.toCharArray();
        for (int i = 0; i < result.length; i++) {
            if (result[i] >= 'A' && result[i] <= 'Z') {
                result[i] += 'a' - 'A';
            }
        }
        return new String(result);
    }
    
    /**
     * Judge whether identifier is a Unicode quoted identifier.
     *
     * @param identifier identifier to be judged
     * @return is Unicode quoted identifier or not
     */
    public static boolean isUnicodeQuoted(final String identifier) {
        return identifier.length() > 3 && ('U' == identifier.charAt(0) || 'u' == identifier.charAt(0)) && '&' == identifier.charAt(1) && '"' == identifier.charAt(2)
                && identifier.lastIndexOf('"') > 2;
    }
    
    /**
     * Unquote Unicode quoted identifier and decode its escape sequences.
     *
     * @param identifier Unicode quoted identifier to be unquoted
     * @return unquoted identifier
     */
    public static String unquoteUnicode(final String identifier) {
        int endQuoteIndex = identifier.lastIndexOf('"');
        String content = identifier.substring(3, endQuoteIndex).replace("\"\"", "\"");
        return decodeUnicodeEscapes(content, getUnicodeEscapeCharacter(identifier.substring(endQuoteIndex + 1)));
    }
    
    private static char getUnicodeEscapeCharacter(final String uescapeClause) {
        Matcher matcher = UESCAPE_CLAUSE_PATTERN.matcher(uescapeClause);
        return matcher.matches() ? matcher.group(1).charAt(0) : DEFAULT_UNICODE_ESCAPE_CHARACTER;
    }
    
    private static String decodeUnicodeEscapes(final String content, final char escapeCharacter) {
        StringBuilder result = new StringBuilder(content.length());
        int index = 0;
        while (index < content.length()) {
            char currentChar = content.charAt(index);
            if (escapeCharacter != currentChar) {
                result.append(currentChar);
                index++;
            } else if (isEscapedEscapeCharacter(content, index, escapeCharacter)) {
                result.append(escapeCharacter);
                index += 2;
            } else {
                index = appendCodePoint(result, content, index);
            }
        }
        return result.toString();
    }
    
    private static boolean isEscapedEscapeCharacter(final String content, final int escapeIndex, final char escapeCharacter) {
        return escapeIndex + 1 < content.length() && escapeCharacter == content.charAt(escapeIndex + 1);
    }
    
    private static int appendCodePoint(final StringBuilder result, final String content, final int escapeIndex) {
        boolean isLongEscape = escapeIndex + 1 < content.length() && '+' == content.charAt(escapeIndex + 1);
        int digitsBeginIndex = isLongEscape ? escapeIndex + 2 : escapeIndex + 1;
        int digitsEndIndex = digitsBeginIndex + (isLongEscape ? LONG_UNICODE_ESCAPE_LENGTH : SHORT_UNICODE_ESCAPE_LENGTH);
        if (digitsEndIndex > content.length()) {
            result.append(content.charAt(escapeIndex));
            return escapeIndex + 1;
        }
        String digits = content.substring(digitsBeginIndex, digitsEndIndex);
        if (!isHexDigits(digits)) {
            result.append(content.charAt(escapeIndex));
            return escapeIndex + 1;
        }
        result.appendCodePoint(Integer.parseInt(digits, 16));
        return digitsEndIndex;
    }
    
    private static boolean isHexDigits(final String digits) {
        for (int i = 0; i < digits.length(); i++) {
            if (-1 == Character.digit(digits.charAt(i), 16)) {
                return false;
            }
        }
        return true;
    }
}
