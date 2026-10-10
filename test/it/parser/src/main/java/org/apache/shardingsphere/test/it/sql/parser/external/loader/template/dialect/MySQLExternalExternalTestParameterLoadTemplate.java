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

package org.apache.shardingsphere.test.it.sql.parser.external.loader.template.dialect;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.SQLLineComment;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * External test parameter load template for MySQL.
 */
public final class MySQLExternalExternalTestParameterLoadTemplate implements ExternalTestParameterLoadTemplate {
    
    private static final int MYSQL_8_0_MAX_VERSION = 80099;
    
    private static final Pattern VERSION_COMMENT_PATTERN = Pattern.compile("/\\*!([0-9]{5}).*?\\*/", Pattern.DOTALL);
    
    private static final Pattern DELIMITER_COMMAND_PATTERN = Pattern.compile("(?i)(?:--\\s*)?delimiter\\s+(.+)");
    
    private static final Pattern CONNECTION_COMMAND_PATTERN = Pattern.compile("(?i)(connection|disconnect)\\s+[^\\s;]+\\s*;?");
    
    private static final Pattern FILE_COMMAND_PATTERN = Pattern.compile("(?i)(?:--\\s*)?(?:write_file|append_file)\\s+\\S+(?:\\s+(\\S+))?");
    
    private static final Pattern PERL_COMMAND_PATTERN = Pattern.compile("(?i)(?:--\\s*)?perl(?:\\s+(\\S+))?");
    
    private static final Pattern LET_COMMAND_PATTERN = Pattern.compile("(?i)let\\s+.*");
    
    private static final Pattern CONTROL_COMMAND_PATTERN = Pattern.compile("(?i)(?:if|while)\\s*\\(.*");
    
    private static final Pattern CHARACTER_SET_COMMAND_PATTERN = Pattern.compile("(?i)(?:--\\s*)?character_set\\s+([^\\s;]+);?");
    
    private static final Map<String, String> CHARSET_NAMES = ImmutableMap.<String, String>builder().put("utf8mb4", "UTF-8").put("utf8mb3", "UTF-8")
            .put("binary", "ISO-8859-1").put("latin2", "ISO-8859-2").put("koi8r", "KOI8-R").put("ujis", "EUC-JP").build();
    
    @Override
    public Charset getContentCharset() {
        return StandardCharsets.ISO_8859_1;
    }
    
    @Override
    public Collection<ExternalSQLTestParameter> load(final String sqlCaseFileName, final List<String> sqlCaseFileContent,
                                                     final List<String> resultFileContent, final String databaseType, final String reportType) {
        Collection<ExternalSQLTestParameter> result = new LinkedList<>();
        List<String> lines = new ArrayList<>();
        String delimiter = ";";
        Charset charset = StandardCharsets.UTF_8;
        int statementState = 0;
        boolean blockComment = false;
        String blockTerminator = null;
        boolean letCommand = false;
        boolean controlCommand = false;
        for (int i = 0; i < sqlCaseFileContent.size(); i++) {
            String line = sqlCaseFileContent.get(i).trim();
            if (null != blockTerminator) {
                if (line.equals(blockTerminator)) {
                    blockTerminator = null;
                }
                continue;
            }
            if (controlCommand) {
                controlCommand = !line.endsWith("{");
                continue;
            }
            if (letCommand) {
                letCommand = !line.endsWith(delimiter);
                continue;
            }
            if (blockComment) {
                blockComment = !line.contains("*/");
                continue;
            }
            if (lines.isEmpty()) {
                String command = line.endsWith(delimiter) ? line.substring(0, line.length() - delimiter.length()).trim() : line;
                Matcher fileMatcher = FILE_COMMAND_PATTERN.matcher(command);
                Matcher perlMatcher = PERL_COMMAND_PATTERN.matcher(command);
                if (fileMatcher.matches()) {
                    blockTerminator = null == fileMatcher.group(1) ? "EOF" : fileMatcher.group(1);
                    continue;
                }
                if (perlMatcher.matches()) {
                    blockTerminator = null == perlMatcher.group(1) ? "EOF" : perlMatcher.group(1);
                    continue;
                }
                if (LET_COMMAND_PATTERN.matcher(line).matches()) {
                    letCommand = !line.endsWith(delimiter);
                    continue;
                }
                if (CONTROL_COMMAND_PATTERN.matcher(line).matches()) {
                    controlCommand = !line.endsWith("{");
                    continue;
                }
                if ("{".equals(line) || "}".equals(line)) {
                    continue;
                }
                Matcher charsetMatcher = CHARACTER_SET_COMMAND_PATTERN.matcher(line);
                if (charsetMatcher.matches()) {
                    String charsetName = charsetMatcher.group(1).toLowerCase(Locale.ROOT);
                    charset = Charset.forName(CHARSET_NAMES.getOrDefault(charsetName, charsetName));
                    continue;
                }
                Matcher delimiterMatcher = DELIMITER_COMMAND_PATTERN.matcher(line);
                if (delimiterMatcher.matches()) {
                    delimiter = getNewDelimiter(delimiterMatcher.group(1), delimiter);
                    continue;
                }
                if (line.startsWith("/*") && !line.contains("*/")) {
                    blockComment = true;
                    continue;
                }
            }
            if (line.isEmpty() || lines.isEmpty() && (SQLLineComment.isComment(line) || CONNECTION_COMMAND_PATTERN.matcher(line).matches())) {
                continue;
            }
            lines.add(line);
            statementState = getStatementState(new String(line.getBytes(StandardCharsets.ISO_8859_1), charset), statementState);
            if (0 == statementState && line.endsWith(delimiter)) {
                if (resultFileContent.isEmpty() || existCorrectResultContent(resultFileContent, lines)) {
                    String sqlCaseId = sqlCaseFileName + ":" + (i + 1);
                    String sql = String.join("\n", lines);
                    sql = sql.substring(0, sql.length() - delimiter.length());
                    result.add(new ExternalSQLTestParameter(sqlCaseId, databaseType, normalizeVersionComments(new String(sql.getBytes(StandardCharsets.ISO_8859_1), charset)), reportType));
                }
                lines.clear();
            }
        }
        return result;
    }
    
    private String normalizeVersionComments(final String sql) {
        StringBuilder result = new StringBuilder(sql);
        Matcher matcher = VERSION_COMMENT_PATTERN.matcher(sql);
        int statementState = 0;
        int previousEnd = 0;
        while (matcher.find()) {
            statementState = getStatementState(sql.substring(previousEnd, matcher.start()), statementState);
            if (0 == statementState && Integer.parseInt(matcher.group(1)) > MYSQL_8_0_MAX_VERSION) {
                result.setCharAt(matcher.start() + 2, ' ');
            }
            statementState = getStatementState(matcher.group(), statementState);
            previousEnd = matcher.end();
        }
        return result.toString();
    }
    
    private String getNewDelimiter(final String sql, final String delimiter) {
        String newDelimiter = sql.substring(0, sql.endsWith(delimiter) ? sql.length() - delimiter.length() : sql.length()).trim();
        if (newDelimiter.startsWith("\"") && newDelimiter.endsWith("\"") || newDelimiter.startsWith("'") && newDelimiter.endsWith("'")) {
            newDelimiter = newDelimiter.substring(1, newDelimiter.length() - 1);
        }
        return newDelimiter.isEmpty() ? delimiter : newDelimiter;
    }
    
    private int getStatementState(final String line, final int statementState) {
        int result = statementState;
        int i = 0;
        while (i < line.length()) {
            char each = line.charAt(i);
            if (-1 == result) {
                if ('*' == each && i + 1 < line.length() && '/' == line.charAt(i + 1)) {
                    result = 0;
                    i++;
                }
            } else if (0 == result && ('#' == each || line.startsWith("--", i) && (i + 2 == line.length() || Character.isWhitespace(line.charAt(i + 2))))) {
                int lineEnd = line.indexOf('\n', i);
                if (-1 == lineEnd) {
                    break;
                }
                i = lineEnd;
            } else if (0 == result && line.startsWith("/*", i)) {
                result = -1;
                i++;
            } else if (0 != result && '\\' == each) {
                i++;
            } else if (0 == result && ('\'' == each || '"' == each || '`' == each)) {
                result = each;
            } else if (each == result) {
                result = 0;
            }
            i++;
        }
        return result;
    }
    
    private boolean existCorrectResultContent(final List<String> resultLines, final List<String> sqlLines) {
        int nextLineIndex = findSQLNextLineIndex(resultLines, sqlLines);
        return -1 != nextLineIndex && (nextLineIndex == resultLines.size() || !resultLines.get(nextLineIndex).contains("ERROR"));
    }
    
    private int findSQLNextLineIndex(final List<String> resultLines, final List<String> sqlLines) {
        int completedSQLIndex = 0;
        for (int resultIndex = 0; resultIndex < resultLines.size(); resultIndex++) {
            if (Objects.equals(sqlLines.get(completedSQLIndex), resultLines.get(resultIndex).trim())) {
                if (++completedSQLIndex == sqlLines.size()) {
                    return resultIndex + 1;
                }
            } else {
                completedSQLIndex = 0;
            }
        }
        return -1;
    }
}
