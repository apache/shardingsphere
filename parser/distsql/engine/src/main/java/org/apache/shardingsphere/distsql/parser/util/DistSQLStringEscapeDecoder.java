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

package org.apache.shardingsphere.distsql.parser.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * DistSQL string escape decoder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLStringEscapeDecoder {
    
    /**
     * Decode standard escapes.
     *
     * @param value value to be decoded
     * @return decoded value
     */
    public static String decode(final String value) {
        if (!value.contains("\\")) {
            return value;
        }
        StringBuilder result = new StringBuilder(value.length());
        int index = 0;
        while (index < value.length()) {
            if ('\\' != value.charAt(index)) {
                result.append(value.charAt(index++));
                continue;
            }
            int backslashEndIndex = index;
            while (backslashEndIndex < value.length() && '\\' == value.charAt(backslashEndIndex)) {
                backslashEndIndex++;
            }
            int backslashCount = backslashEndIndex - index;
            int backslashPairCount = backslashCount / 2;
            while (0 < backslashPairCount--) {
                result.append('\\');
            }
            if (0 != backslashCount % 2) {
                if (backslashEndIndex < value.length() && appendStandardEscape(value.charAt(backslashEndIndex), result)) {
                    backslashEndIndex++;
                } else {
                    result.append('\\');
                }
            }
            index = backslashEndIndex;
        }
        return result.toString();
    }
    
    private static boolean appendStandardEscape(final char value, final StringBuilder result) {
        switch (value) {
            case 'b':
                result.append('\b');
                break;
            case 't':
                result.append('\t');
                break;
            case 'n':
                result.append('\n');
                break;
            case 'f':
                result.append('\f');
                break;
            case 'r':
                result.append('\r');
                break;
            case 's':
                result.append(' ');
                break;
            case '"':
            case '\'':
                result.append(value);
                break;
            default:
                return false;
        }
        return true;
    }
}
