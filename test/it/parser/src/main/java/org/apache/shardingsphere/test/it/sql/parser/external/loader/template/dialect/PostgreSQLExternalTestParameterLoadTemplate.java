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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.spi.DialectSQLParserFacade;
import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * External test parameter load template for PostgreSQL.
 */
public final class PostgreSQLExternalTestParameterLoadTemplate implements ExternalTestParameterLoadTemplate {
    
    @Override
    public Collection<ExternalSQLTestParameter> load(final String sqlCaseFileName, final List<String> sqlCaseFileContent,
                                                     final List<String> resultFileContent, final String databaseType, final String reportType) {
        List<ScriptStatement> statements = new ScriptReader(sqlCaseFileContent, databaseType).read();
        int[] echoes = findEchoes(statements, resultFileContent);
        Collection<ExternalSQLTestParameter> result = new ArrayList<>(statements.size());
        for (int i = 0; i < statements.size(); i++) {
            ScriptStatement statement = statements.get(i);
            if (!statement.sql.isEmpty() && !hasNativeError(statement, i, echoes, resultFileContent)) {
                result.add(new ExternalSQLTestParameter(sqlCaseFileName + (result.size() + 1), databaseType, statement.sql, reportType));
            }
        }
        return result;
    }
    
    private int[] findEchoes(final List<ScriptStatement> statements, final List<String> expectedLines) {
        Map<String, List<Integer>> linePositions = IntStream.range(0, expectedLines.size()).boxed().collect(Collectors.groupingBy(expectedLines::get));
        Map<List<String>, Integer> occurrences = statements.stream().collect(Collectors.toMap(each -> each.echo, each -> 1, Integer::sum));
        Map<List<String>, Iterator<Integer>> matches = occurrences.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> matchEcho(entry.getKey(), linePositions, expectedLines, entry.getValue()).iterator()));
        int[] result = new int[statements.size()];
        Arrays.fill(result, -1);
        int cursor = 0;
        for (int i = 0; i < statements.size(); i++) {
            List<String> echo = statements.get(i).echo;
            Iterator<Integer> positions = matches.get(echo);
            int position = positions.hasNext() ? positions.next() : -1;
            if (position >= cursor) {
                result[i] = position;
                cursor = result[i] + echo.size();
            }
        }
        return result;
    }
    
    private List<Integer> matchEcho(final List<String> echo, final Map<String, List<Integer>> linePositions, final List<String> expectedLines, final int occurrences) {
        Collection<Integer> positions = linePositions.getOrDefault(echo.get(0), Collections.emptyList());
        List<Integer> result = positions.stream().mapToInt(each -> each)
                .filter(each -> each + echo.size() <= expectedLines.size() && expectedLines.subList(each, each + echo.size()).equals(echo)).boxed().collect(Collectors.toList());
        return result.size() == occurrences ? result : Collections.emptyList();
    }
    
    private boolean hasNativeError(final ScriptStatement statement, final int index, final int[] echoes, final List<String> expectedLines) {
        if (!statement.filterable || echoes[index] < 0 || index + 1 < echoes.length && echoes[index + 1] < 0) {
            return false;
        }
        int end = index + 1 == echoes.length ? expectedLines.size() : echoes[index + 1];
        for (int i = echoes[index] + statement.echo.size(); i < end; i++) {
            String line = expectedLines.get(i);
            if (line.startsWith("NOTICE:") || line.startsWith("WARNING:") || line.startsWith("INFO:") || line.startsWith("LOG:") || line.startsWith("DEBUG:")) {
                return false;
            }
            if (line.startsWith("ERROR:")) {
                return true;
            }
        }
        return false;
    }
    
    @RequiredArgsConstructor
    private static final class ScriptStatement {
        
        private final String sql;
        
        private final List<String> echo;
        
        private final boolean filterable;
    }
    
    private static final class ScriptReader extends BaseErrorListener {
        
        private final List<String> lines;
        
        private final DatabaseType databaseType;
        
        private final CharStream input;
        
        private final Lexer lexer;
        
        private final int[] lineStarts;
        
        private final boolean[] quotedLines;
        
        private final List<ScriptStatement> statements = new ArrayList<>();
        
        private int start = -1;
        
        private int parentheses;
        
        private int atomicBlocks;
        
        private int caseBlocks;
        
        private String previousKind = "";
        
        private boolean firstToken = true;
        
        private boolean copyStatement;
        
        private boolean copyOutput;
        
        private boolean pendingCopy;
        
        private boolean unsafe;
        
        private boolean clientBuffer;
        
        private boolean echoAll = true;
        
        private boolean clientStateKnown = true;
        
        private ScriptReader(final List<String> lines, final String databaseType) {
            this.lines = lines;
            this.databaseType = TypedSPILoader.getService(DatabaseType.class, databaseType);
            input = CodePointCharStream.fromBuffer(CodePointBuffer.withChars(CharBuffer.wrap(String.join("\n", lines).toCharArray())));
            lexer = createLexer(input);
            lexer.addErrorListener(this);
            lineStarts = new int[lines.size()];
            quotedLines = new boolean[lines.size()];
            for (int i = 1; i < lineStarts.length; i++) {
                lineStarts[i] = lineStarts[i - 1] + lines.get(i - 1).length() + 1;
            }
        }
        
        @SneakyThrows(ReflectiveOperationException.class)
        private Lexer createLexer(final CharStream characters) {
            Lexer result = (Lexer) DatabaseTypedSPILoader.getService(DialectSQLParserFacade.class, databaseType).getLexerClass().getConstructor(CharStream.class).newInstance(characters);
            result.removeErrorListener(ConsoleErrorListener.INSTANCE);
            return result;
        }
        
        private List<ScriptStatement> read() {
            for (Token token = lexer.nextToken(); Token.EOF != token.getType(); token = lexer.nextToken()) {
                markQuotedLines(token);
                if (Token.DEFAULT_CHANNEL != token.getChannel()) {
                    if (start < 0) {
                        statements.add(new ScriptStatement("", getEcho(lineAt(token.getStartIndex()), lineAt(token.getStopIndex())), false));
                    }
                    continue;
                }
                String kind = lexer.getVocabulary().getSymbolicName(token.getType());
                if ("BACKSLASH_".equals(kind) && readCommand(token.getStartIndex())) {
                    continue;
                }
                if (start < 0) {
                    start = token.getStartIndex();
                }
                readBlock(kind, token);
                readCopy(kind);
                previousKind = kind;
                if ("COLON_".equals(kind)) {
                    unsafe = true;
                }
                if ("SEMI_".equals(kind) && (clientBuffer || 0 == parentheses && 0 == atomicBlocks)) {
                    submit(token.getStopIndex() + 1, token.getStopIndex() + 1, true);
                }
            }
            if (start >= 0) {
                submit(input.size(), input.size(), 0 == parentheses && 0 == atomicBlocks);
            }
            return statements;
        }
        
        private void markQuotedLines(final Token token) {
            if (lexer.getLine() > token.getLine()) {
                int lastLine = lineAt(token.getStopIndex());
                for (int i = token.getLine(); i <= lastLine; i++) {
                    quotedLines[i] = true;
                }
            }
        }
        
        private List<String> getEcho(final int firstLine, final int lastLine) {
            return IntStream.rangeClosed(firstLine, lastLine).filter(i -> !lines.get(i).isEmpty() || quotedLines[i]).mapToObj(lines::get).collect(Collectors.toList());
        }
        
        private boolean readCommand(final int offset) {
            int line = lineAt(offset);
            int end = lineStarts[line] + lines.get(line).length();
            String command = input.getText(Interval.of(offset + 1, end - 1));
            if (command.startsWith(";")) {
                unsafe = true;
                clientBuffer = true;
                firstToken = true;
                previousKind = "";
                seek(offset + 2);
                return true;
            }
            String name = command.split("\\s|\\\\", 2)[0];
            if (command.indexOf('\\') >= 0) {
                unsafe = true;
                clientBuffer = true;
                return false;
            }
            if (Arrays.asList("g", "gx", "gset", "gexec").contains(name)) {
                boolean copy = pendingCopy;
                if (start >= 0) {
                    submit(offset, end, "g".equals(command.trim()) || "gset".equals(command.trim()));
                } else {
                    statements.add(new ScriptStatement("", lines.subList(line, line + 1), false));
                }
                if (!copy) {
                    seek(Math.min(input.size(), end + 1));
                }
                return true;
            }
            if (start >= 0) {
                unsafe = true;
                clientBuffer = true;
                return false;
            }
            if ("copy".equals(name)) {
                start = offset;
                unsafe = true;
                boolean copy = isCopyFromStdin(command);
                pendingCopy = copy;
                submit(end, end, false);
                if (!copy) {
                    seek(Math.min(input.size(), end + 1));
                }
                return true;
            }
            statements.add(new ScriptStatement("", lines.subList(line, line + 1), false));
            if (command.startsWith("set ECHO ")) {
                echoAll = "all".equals(command.substring("set ECHO ".length()).trim());
            } else if (!Arrays.asList("echo", "qecho").contains(name)) {
                clientStateKnown = false;
            }
            seek(Math.min(input.size(), end + 1));
            return true;
        }
        
        private boolean isCopyFromStdin(final String command) {
            Lexer header = createLexer(CodePointCharStream.fromBuffer(CodePointBuffer.withChars(CharBuffer.wrap(command.toCharArray()))));
            int depth = 0;
            boolean from = false;
            for (Token token = header.nextToken(); Token.EOF != token.getType(); token = header.nextToken()) {
                if (Token.DEFAULT_CHANNEL != token.getChannel()) {
                    continue;
                }
                String kind = header.getVocabulary().getSymbolicName(token.getType());
                if ("LP_".equals(kind)) {
                    depth++;
                } else if ("RP_".equals(kind)) {
                    depth--;
                } else if (0 == depth && "STDIN".equals(kind) && from) {
                    return true;
                }
                from = "FROM".equals(kind);
            }
            return false;
        }
        
        private void seek(final int offset) {
            input.seek(offset);
            if (offset < input.size()) {
                int line = lineAt(offset);
                lexer.setLine(line + 1);
                lexer.setCharPositionInLine(offset - lineStarts[line]);
            }
        }
        
        private int lineAt(final int offset) {
            int index = Arrays.binarySearch(lineStarts, offset);
            return index >= 0 ? index : -index - 2;
        }
        
        private void readBlock(final String kind, final Token token) {
            if ("LP_".equals(kind)) {
                parentheses++;
            } else if ("RP_".equals(kind) && parentheses > 0) {
                parentheses--;
            } else if ("BEGIN".equals(previousKind) && "ATOMIC".equalsIgnoreCase(token.getText())) {
                atomicBlocks++;
            } else if (atomicBlocks > 0 && "CASE".equals(kind)) {
                caseBlocks++;
            } else if (atomicBlocks > 0 && "END".equals(kind)) {
                if (caseBlocks > 0) {
                    caseBlocks--;
                } else {
                    atomicBlocks--;
                }
            }
        }
        
        private void readCopy(final String kind) {
            if (firstToken) {
                copyStatement = "COPY".equals(kind);
                firstToken = false;
            }
            if (copyStatement && 0 == parentheses && "FROM".equals(previousKind) && "STDIN".equals(kind)) {
                pendingCopy = true;
            }
            if (copyStatement && 0 == parentheses && "TO".equals(previousKind) && "STDOUT".equals(kind)) {
                copyOutput = true;
            }
        }
        
        private void submit(final int sqlEnd, final int echoEnd, final boolean filterable) {
            int end = sqlEnd;
            int firstLine = lineAt(start);
            int lastLine = lineAt(echoEnd - 1);
            boolean exclusive = input.getText(Interval.of(lineStarts[firstLine], start - 1)).trim().isEmpty()
                    && input.getText(Interval.of(echoEnd, lineStarts[lastLine] + lines.get(lastLine).length() - 1)).trim().isEmpty();
            if (pendingCopy) {
                int marker = IntStream.range(lastLine + 1, lines.size()).filter(i -> "\\.".equals(lines.get(i))).findFirst().orElse(-1);
                if (marker < 0) {
                    end = input.size();
                    unsafe = true;
                    seek(input.size());
                } else {
                    seek(Math.min(input.size(), lineStarts[marker] + lines.get(marker).length() + 1));
                }
            }
            statements.add(new ScriptStatement(input.getText(Interval.of(start, end - 1)).trim(), getEcho(firstLine, lastLine),
                    filterable && exclusive && !unsafe && !copyOutput && echoAll && clientStateKnown));
            reset();
        }
        
        private void reset() {
            start = -1;
            parentheses = 0;
            atomicBlocks = 0;
            caseBlocks = 0;
            previousKind = "";
            firstToken = true;
            copyStatement = false;
            copyOutput = false;
            pendingCopy = false;
            unsafe = false;
            clientBuffer = false;
        }
        
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg, final RecognitionException ex) {
            unsafe = true;
            if (start < 0) {
                start = ((LexerNoViableAltException) ex).getStartIndex();
            }
        }
    }
}
