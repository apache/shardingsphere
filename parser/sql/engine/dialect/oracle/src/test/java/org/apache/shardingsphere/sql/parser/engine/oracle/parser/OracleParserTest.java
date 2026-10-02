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

package org.apache.shardingsphere.sql.parser.engine.oracle.parser;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.atn.DecisionInfo;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Measures cold and warm shared Oracle parser DFA caches")
class OracleParserTest {
    
    private static final int MAX_ADAPTIVE_LOOKAHEAD = 16;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("concatenationArguments")
    void assertParseLongConcatenation(final String name, final String expression, final int count, final boolean warm) {
        assertBoundedPrediction("SELECT " + String.join("||CHR(124)||", Collections.nCopies(count, expression)) + " FROM foo_table", warm);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("longProjectionArguments")
    void assertParseLongProjection(final String name, final String sql) {
        assertBoundedPrediction(sql, false);
    }
    
    private void assertBoundedPrediction(final String sql, final boolean warm) {
        OracleParser parser = createParser(sql, PredictionMode.SLL);
        parser.getInterpreter().clearDFA();
        if (warm) {
            parser.parse();
            parser.reset();
        }
        parser.setProfile(true);
        assertNotNull(parser.parse());
        assertComplete(parser);
        DecisionInfo[] decisions = parser.getParseInfo().getDecisionInfo();
        assertTrue(Arrays.stream(decisions).allMatch(each -> each.SLL_MaxLook <= MAX_ADAPTIVE_LOOKAHEAD), "Prediction scanned across concatenation operands");
        assertTrue(Arrays.stream(decisions).mapToLong(each -> each.SLL_TotalLook).sum() <= (long) parser.getTokenStream().size() * MAX_ADAPTIVE_LOOKAHEAD,
                "Total prediction work exceeded the linear token budget");
    }
    
    private static Stream<Arguments> concatenationArguments() {
        return Stream.of("foo_function(foo_column)", "TO_CHAR(foo_column,'YYYY-MM-DD')", "foo_function(bar_function(foo_column))",
                "foo_schema.foo_function(foo_column)", "CAST(foo_column AS VARCHAR2(20))")
                .flatMap(expression -> IntStream.of(24, 48, 96).boxed().flatMap(count -> Stream.of(false, true)
                        .map(warm -> Arguments.of(expression + "/" + count + (warm ? "/warm" : "/cold"), expression, count, warm))));
    }
    
    private static Stream<Arguments> longProjectionArguments() {
        String repeated = String.join("||CHR(124)||", Collections.nCopies(54, "COL_VARCHAR2"));
        Collection<Integer> dateColumns = Arrays.asList(12, 17, 22, 38, 39, 40);
        String mixed = IntStream.range(0, 54).mapToObj(each -> dateColumns.contains(each)
                ? "TO_CHAR(COL_" + each + ", 'YYYY-MM-DD HH24:MI:SS')"
                : "COL_" + each).collect(Collectors.joining("||CHR(124)||"));
        return Stream.of(Arguments.of("unaliased 54-field projection", "SELECT " + repeated + " FROM foo_schema.foo_table WHERE id = 1 AND rownum < 2"),
                Arguments.of("aliased 54-field projection", "SELECT " + repeated + " AS EXPORT_ROW FROM foo_schema.foo_table WHERE id = 1 AND rownum < 2"),
                Arguments.of("six TO_CHAR calls in 54 fields", "SELECT " + mixed + " FROM foo_table WHERE id = 1 AND rownum < 2"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validStatements")
    void assertParseExpressionBoundaries(final String name, final String sql, final PredictionMode mode) {
        OracleParser parser = createParser(sql, mode);
        assertNotNull(parser.parse());
        assertComplete(parser);
    }
    
    private static Stream<Arguments> validStatements() {
        return sqlArguments(
                "FLASHBACK DATABASE TO TIMESTAMP SYSDATE-1",
                "FLASHBACK TABLE foo_table TO TIMESTAMP (SYSTIMESTAMP - INTERVAL '1' MINUTE)",
                "SELECT s FROM foo_table MODEL DIMENSION BY (d) MEASURES (s) RULES (s[1] = s[2] + (s[3] + 1))",
                "SELECT s FROM foo_table MODEL DIMENSION BY (d) MEASURES (s) RULES (s[1] = s[2] / 2)",
                "BEGIN EXECUTE IMMEDIATE 'BEGIN NULL; END;' USING IN a, OUT b; END;",
                "BEGIN OPEN foo_cursor(1,2); END;",
                "BEGIN FOR r IN foo_cursor(1,2) LOOP NULL; END LOOP; END;",
                "DECLARE v foo_record := foo_record(1,b=>2); BEGIN NULL; END;",
                "DECLARE v foo_record := foo_record(a=>1,b=>2); BEGIN NULL; END;",
                "DECLARE v foo_record := foo_record(); BEGIN NULL; END;",
                "BEGIN FORALL i IN 1..2 INSERT INTO foo_table VALUES (v(i)); END;",
                "BEGIN FORALL i IN 1..2 UPDATE foo_table SET id = v(i); END;",
                "BEGIN FORALL i IN 1..2 DELETE FROM foo_table WHERE id = v(i); END;",
                "BEGIN FORALL i IN 1..2 MERGE INTO foo_table t USING bar_table s ON (t.id=s.id) WHEN MATCHED THEN UPDATE SET t.val=v(i); END;",
                "BEGIN FORALL i IN 1..v.COUNT SAVE EXCEPTIONS EXECUTE IMMEDIATE 'DELETE FROM foo_table WHERE id=:1' USING v(i); END;");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStatements")
    void assertRejectAdjacentExpressions(final String name, final String sql, final PredictionMode mode) {
        assertThrows(ParseCancellationException.class, () -> createParser(sql, mode).parse());
    }
    
    private static Stream<Arguments> invalidStatements() {
        return sqlArguments(
                "FLASHBACK DATABASE TO TIMESTAMP SYSDATE SYSDATE",
                "FLASHBACK DATABASE TO TIMESTAMP (SYSDATE",
                "SELECT s FROM foo_table MODEL DIMENSION BY (d) MEASURES (s) RULES (s[1] = s[2] + 1 2)",
                "SELECT s FROM foo_table MODEL DIMENSION BY (d) MEASURES (s) RULES (s[1] = s[2] +)",
                "SELECT s FROM foo_table MODEL DIMENSION BY (d) MEASURES (s) RULES (s[1] = s[2] /)",
                "BEGIN EXECUTE IMMEDIATE 'BEGIN NULL; END;' USING a b; END;",
                "BEGIN EXECUTE IMMEDIATE 'BEGIN NULL; END;' USING IN a OUT b; END;",
                "BEGIN OPEN foo_cursor(1 2); END;",
                "BEGIN FOR r IN foo_cursor(1 2) LOOP NULL; END LOOP; END;",
                "DECLARE v foo_record := foo_record(1 b=>2); BEGIN NULL; END;",
                "BEGIN FORALL i IN 1..v.COUNT 'DELETE FROM foo_table'; END;",
                "BEGIN FORALL i IN 1..v.COUNT (v(i)); END;");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validDynamicSql")
    void assertDynamicSqlArguments(final String name, final String sql, final PredictionMode mode) {
        OracleParser parser = createParser(sql, mode);
        parser.dynamicSql();
        assertComplete(parser);
    }
    
    private static Stream<Arguments> validDynamicSql() {
        return sqlArguments("EXECUTE IMMEDIATE 'SELECT 1 FROM DUAL'", "EXECUTE IMMEDIATE 'SELECT :a+:b FROM DUAL' USING IN a,b");
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDynamicSql")
    void assertRejectDynamicSqlArguments(final String name, final String sql, final PredictionMode mode) {
        OracleParser parser = createParser(sql, mode);
        try {
            parser.dynamicSql();
        } catch (final ParseCancellationException ignored) {
            return;
        }
        assertThat(parser.getCurrentToken().getType(), not(Token.EOF));
    }
    
    private static Stream<Arguments> invalidDynamicSql() {
        return sqlArguments("EXECUTE IMMEDIATE 'SELECT :a+:b FROM DUAL' USING a b", "EXECUTE IMMEDIATE 'SELECT 1 FROM DUAL' USING",
                "EXECUTE IMMEDIATE 'SELECT :a FROM DUAL' USING a,");
    }
    
    private static Stream<Arguments> sqlArguments(final String... sql) {
        return Arrays.stream(sql).flatMap(each -> Stream.of(PredictionMode.SLL, PredictionMode.LL).map(mode -> Arguments.of(mode + ": " + each, each, mode)));
    }
    
    private OracleParser createParser(final String sql, final PredictionMode mode) {
        OracleLexer lexer = new OracleLexer(CharStreams.fromString(sql));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {
            
            @Override
            public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine,
                                    final String msg, final RecognitionException ex) {
                throw new ParseCancellationException(msg);
            }
        });
        OracleParser result = new OracleParser(new BoundedTokenStream(lexer, sql.length() * 128));
        result.setErrorHandler(new BailErrorStrategy());
        result.removeErrorListeners();
        result.getInterpreter().setPredictionMode(mode);
        return result;
    }
    
    private void assertComplete(final OracleParser parser) {
        assertThat(parser.getNumberOfSyntaxErrors(), is(0));
        assertThat(parser.getCurrentToken().getType(), is(Token.EOF));
    }
    
    private static final class BoundedTokenStream extends CommonTokenStream {
        
        private final int maximumLookahead;
        
        private int lookaheadCount;
        
        private BoundedTokenStream(final TokenSource tokenSource, final int maximumLookahead) {
            super(tokenSource);
            this.maximumLookahead = maximumLookahead;
        }
        
        @Override
        public Token LT(final int k) {
            if (++lookaheadCount > maximumLookahead) {
                throw new AssertionError("Token lookahead exceeded the linear budget of " + maximumLookahead);
            }
            return super.LT(k);
        }
    }
}
