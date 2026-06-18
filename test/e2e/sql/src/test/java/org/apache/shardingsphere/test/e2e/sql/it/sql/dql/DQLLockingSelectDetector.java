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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dql;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Locking SELECT detector for SQL E2E tests.
 */
public final class DQLLockingSelectDetector {
    
    private static final Pattern SELECT_PATTERN = Pattern.compile("\\bSELECT\\b");
    
    private static final Pattern FOR_UPDATE_PATTERN = Pattern.compile("\\bFOR\\s+(?:NO\\s+KEY\\s+)?UPDATE\\b");
    
    private static final Pattern FOR_SHARE_PATTERN = Pattern.compile("\\bFOR\\s+(?:KEY\\s+)?SHARE\\b");
    
    private static final Pattern LOCK_IN_SHARE_MODE_PATTERN = Pattern.compile("\\bLOCK\\s+IN\\s+SHARE\\s+MODE\\b");
    
    private static final Pattern SQL_SERVER_LOCK_HINT_PATTERN = Pattern.compile(
            "\\bWITH\\s*\\([^)]*\\b(?:UPDLOCK|HOLDLOCK|XLOCK|TABLOCKX|TABLOCK|PAGLOCK|ROWLOCK|READCOMMITTEDLOCK|REPEATABLEREAD|SERIALIZABLE)\\b[^)]*\\)");
    
    private static final Pattern KEEP_LOCKS_PATTERN = Pattern.compile("\\bWITH\\s+(?:RR|RS|CS)\\s+USE\\s+AND\\s+KEEP\\s+(?:UPDATE|EXCLUSIVE|SHARE)\\s+LOCKS\\b");
    
    private static final Pattern WITH_LOCK_PATTERN = Pattern.compile("\\bWITH\\s+LOCK\\b(?!\\s+AS\\b)");
    
    private DQLLockingSelectDetector() {
    }
    
    /**
     * Judge whether SQL contains SELECT locking syntax.
     *
     * @param sql SQL
     * @return contains SELECT locking syntax or not
     */
    public static boolean containsLockingSelect(final String sql) {
        String normalizedSQL = normalize(sql);
        return SELECT_PATTERN.matcher(normalizedSQL).find() && (FOR_UPDATE_PATTERN.matcher(normalizedSQL).find() || FOR_SHARE_PATTERN.matcher(normalizedSQL).find()
                || LOCK_IN_SHARE_MODE_PATTERN.matcher(normalizedSQL).find() || SQL_SERVER_LOCK_HINT_PATTERN.matcher(normalizedSQL).find()
                || KEEP_LOCKS_PATTERN.matcher(normalizedSQL).find() || WITH_LOCK_PATTERN.matcher(normalizedSQL).find());
    }
    
    private static String normalize(final String sql) {
        StringBuilder result = new StringBuilder(sql.length());
        int i = 0;
        while (i < sql.length()) {
            char each = sql.charAt(i);
            if ('\'' == each || '"' == each || '`' == each) {
                result.append(' ');
                i = skipQuotedIdentifier(sql, i, each);
            } else if ('[' == each) {
                result.append(' ');
                i = skipSquareQuotedIdentifier(sql, i);
            } else if ('-' == each && i + 1 < sql.length() && '-' == sql.charAt(i + 1)) {
                result.append(' ');
                i = skipLineComment(sql, i + 2);
            } else if ('#' == each) {
                result.append(' ');
                i = skipLineComment(sql, i + 1);
            } else if ('/' == each && i + 1 < sql.length() && '*' == sql.charAt(i + 1)) {
                result.append(' ');
                i = skipBlockComment(sql, i);
            } else {
                result.append(each);
            }
            i++;
        }
        return result.toString().toUpperCase(Locale.ENGLISH).replaceAll("\\s+", " ");
    }
    
    private static int skipQuotedIdentifier(final String sql, final int startIndex, final char quoteCharacter) {
        int i = startIndex + 1;
        while (i < sql.length()) {
            if ('\\' == sql.charAt(i)) {
                i++;
            } else if (quoteCharacter == sql.charAt(i)) {
                if (i + 1 < sql.length() && quoteCharacter == sql.charAt(i + 1)) {
                    i++;
                } else {
                    return i;
                }
            }
            i++;
        }
        return sql.length() - 1;
    }
    
    private static int skipSquareQuotedIdentifier(final String sql, final int startIndex) {
        int i = startIndex + 1;
        while (i < sql.length()) {
            if (']' == sql.charAt(i)) {
                return i;
            }
            i++;
        }
        return sql.length() - 1;
    }
    
    private static int skipLineComment(final String sql, final int startIndex) {
        int i = startIndex;
        while (i < sql.length()) {
            if ('\n' == sql.charAt(i)) {
                return i;
            }
            i++;
        }
        return sql.length() - 1;
    }
    
    private static int skipBlockComment(final String sql, final int startIndex) {
        int i = startIndex + 2;
        while (i < sql.length() - 1) {
            if ('*' == sql.charAt(i) && '/' == sql.charAt(i + 1)) {
                return i + 1;
            }
            i++;
        }
        return sql.length() - 1;
    }
}
