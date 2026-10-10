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

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLExternalTestParameterLoadTemplateTest {
    
    private final PostgreSQLExternalTestParameterLoadTemplate template = new PostgreSQLExternalTestParameterLoadTemplate();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource({"sqlCases", "expectedCases"})
    void assertLoadSQL(final String name, final String source, final String expectedEcho, final Collection<String> expectedSQL) {
        List<String> expectedFileContent = expectedEcho.isEmpty() ? Collections.emptyList() : Arrays.asList(expectedEcho.split("\n", -1));
        Collection<ExternalSQLTestParameter> actual = template.load("sample", Arrays.asList(source.split("\n", -1)), expectedFileContent, "PostgreSQL", "CSV");
        assertThat(actual.stream().map(ExternalSQLTestParameter::getSql).collect(Collectors.toList()), is(expectedSQL));
        assertThat(actual.stream().map(ExternalSQLTestParameter::getSqlCaseId).collect(Collectors.toList()),
                is(IntStream.rangeClosed(1, expectedSQL.size()).mapToObj(index -> "sample" + index).collect(Collectors.toList())));
    }
    
    private static Stream<Arguments> sqlCases() {
        return Stream.of(
                Arguments.of("tagged dollar body", "DO $x$\nDECLARE\n  foo integer;\nBEGIN\n  foo := 1;\nEND;\n$x$;\nSELECT 2;", "",
                        Arrays.asList("DO $x$\nDECLARE\n  foo integer;\nBEGIN\n  foo := 1;\nEND;\n$x$;", "SELECT 2;")),
                Arguments.of("nested dollar text", "DO $x$ BEGIN PERFORM $y$foo;bar$y$; END $x$; SELECT 2;", "", Arrays.asList("DO $x$ BEGIN PERFORM $y$foo;bar$y$; END $x$;", "SELECT 2;")),
                Arguments.of("untagged dollar body", "CREATE FUNCTION foo() RETURNS integer AS $$ BEGIN RETURN 1; END; $$ LANGUAGE plpgsql; SELECT 2;", "",
                        Arrays.asList("CREATE FUNCTION foo() RETURNS integer AS $$ BEGIN RETURN 1; END; $$ LANGUAGE plpgsql;", "SELECT 2;")),
                Arguments.of("single quoted body", "CREATE FUNCTION foo() RETURNS integer AS 'BEGIN\nRETURN 1;\nEND' LANGUAGE plpgsql;\nSELECT 2;", "",
                        Arrays.asList("CREATE FUNCTION foo() RETURNS integer AS 'BEGIN\nRETURN 1;\nEND' LANGUAGE plpgsql;", "SELECT 2;")),
                Arguments.of("escaped single quote", "SELECT 'foo'';bar'; SELECT 2;", "", Arrays.asList("SELECT 'foo'';bar';", "SELECT 2;")),
                Arguments.of("escape string", "SELECT E'foo\\';bar'; SELECT 2;", "", Arrays.asList("SELECT E'foo\\';bar';", "SELECT 2;")),
                Arguments.of("quoted identifier", "SELECT \"foo;\"\"bar\"; SELECT 2;", "", Arrays.asList("SELECT \"foo;\"\"bar\";", "SELECT 2;")),
                Arguments.of("line comment in statement", "SELECT\n--foo;bar\n1;\nSELECT 2;", "", Arrays.asList("SELECT\n--foo;bar\n1;", "SELECT 2;")),
                Arguments.of("nested block comment", "SELECT /* foo; /* bar; */ baz; */ 1; SELECT 2;", "", Arrays.asList("SELECT /* foo; /* bar; */ baz; */ 1;", "SELECT 2;")),
                Arguments.of("blank line in statement", "SELECT\n\n  -- foo\n  1;\nSELECT 2;", "", Arrays.asList("SELECT\n\n  -- foo\n  1;", "SELECT 2;")),
                Arguments.of("blank lines in quoted text", "SELECT 'foo\n\nbar', \"foo\n\nbar\", $x$foo\n\nbar$x$ /*foo\n\nbar*/;\nSELECT 2;", "",
                        Arrays.asList("SELECT 'foo\n\nbar', \"foo\n\nbar\", $x$foo\n\nbar$x$ /*foo\n\nbar*/;", "SELECT 2;")),
                Arguments.of("leading comments", "-- foo;\n/* bar; */\nSELECT 1;", "", Collections.singletonList("SELECT 1;")),
                Arguments.of("same line statements", "SELECT 1; SELECT 2; SELECT 3;", "", Arrays.asList("SELECT 1;", "SELECT 2;", "SELECT 3;")),
                Arguments.of("atomic body with case", "CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT CASE WHEN true THEN 1 ELSE 2 END; SELECT 3; END; SELECT 4;", "",
                        Arrays.asList("CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT CASE WHEN true THEN 1 ELSE 2 END; SELECT 3; END;", "SELECT 4;")),
                Arguments.of("transaction begin", "BEGIN; SELECT 1; COMMIT;", "", Arrays.asList("BEGIN;", "SELECT 1;", "COMMIT;")),
                Arguments.of("unmatched closing parenthesis", "SELECT );\nSELECT 2;", "", Arrays.asList("SELECT );", "SELECT 2;")),
                Arguments.of("rule action parentheses", "CREATE RULE foo AS ON INSERT TO bar DO ALSO (INSERT INTO baz VALUES (1); INSERT INTO baz VALUES (2)); SELECT 3;", "",
                        Arrays.asList("CREATE RULE foo AS ON INSERT TO bar DO ALSO (INSERT INTO baz VALUES (1); INSERT INTO baz VALUES (2));", "SELECT 3;")),
                Arguments.of("submit g", "SELECT 1 \\g\nSELECT 2;", "", Arrays.asList("SELECT 1", "SELECT 2;")),
                Arguments.of("submit gx", "SELECT 1 \\gx\nSELECT 2;", "", Arrays.asList("SELECT 1", "SELECT 2;")),
                Arguments.of("submit gset", "SELECT 1 \\gset\nSELECT 2;", "", Arrays.asList("SELECT 1", "SELECT 2;")),
                Arguments.of("submit gexec", "SELECT 1 \\gexec\nSELECT 2;", "", Arrays.asList("SELECT 1", "SELECT 2;")),
                Arguments.of("multiline submit", "SELECT\n  1\n\\gset\nSELECT 2;", "", Arrays.asList("SELECT\n  1", "SELECT 2;")),
                Arguments.of("copy payload", "COPY foo FROM STDIN;\nfoo';\n$x$;\n\\.\nSELECT 2;", "", Arrays.asList("COPY foo FROM STDIN;", "SELECT 2;")),
                Arguments.of("copy payload until eof", "COPY foo FROM STDIN;\nfoo';\n$x$;", "", Collections.singletonList("COPY foo FROM STDIN;\nfoo';\n$x$;")),
                Arguments.of("copy submitted with g", "COPY foo FROM STDIN \\g\nfoo';\n$x$;\n\\.\nSELECT 2;", "", Arrays.asList("COPY foo FROM STDIN", "SELECT 2;")),
                Arguments.of("copy submitted with g until eof", "COPY foo FROM STDIN \\g\nfoo';\n$x$;", "", Collections.singletonList("COPY foo FROM STDIN \\g\nfoo';\n$x$;")),
                Arguments.of("client copy payload", "\\copy foo FROM STDIN\nfoo';\n$x$;\n\\.\nSELECT 2;", "", Arrays.asList("\\copy foo FROM STDIN", "SELECT 2;")),
                Arguments.of("client copy payload until eof", "\\copy foo FROM STDIN\nfoo';\n$x$;", "", Collections.singletonList("\\copy foo FROM STDIN\nfoo';\n$x$;")),
                Arguments.of("client copy query with stdin table", "\\copy (SELECT * FROM stdin /*comment*/) TO STDOUT;\nSELECT 2;", "",
                        Arrays.asList("\\copy (SELECT * FROM stdin /*comment*/) TO STDOUT;", "SELECT 2;")),
                Arguments.of("client copy stdin table", "\\copy stdin TO STDOUT\nSELECT 2;", "", Arrays.asList("\\copy stdin TO STDOUT", "SELECT 2;")),
                Arguments.of("copy in multiple query buffer", "SELECT 1 \\; COPY foo FROM STDIN;\nfoo';\n$x$;\n\\.\nSELECT 2;", "", Arrays.asList("SELECT 1 \\; COPY foo FROM STDIN;", "SELECT 2;")),
                Arguments.of("copy in multiple query buffer until eof", "SELECT 1 \\; COPY foo FROM STDIN;\nfoo';\n$x$;", "",
                        Collections.singletonList("SELECT 1 \\; COPY foo FROM STDIN;\nfoo';\n$x$;")),
                Arguments.of("stdin table in copy query", "COPY (SELECT * FROM stdin) TO STDOUT;\nSELECT 2;", "", Arrays.asList("COPY (SELECT * FROM stdin) TO STDOUT;", "SELECT 2;")),
                Arguments.of("copy file paths", "COPY foo FROM '/tmp/in';\nCOPY foo TO '/tmp/out';\nSELECT 3;", "", Arrays.asList("COPY foo FROM '/tmp/in';", "COPY foo TO '/tmp/out';", "SELECT 3;")),
                Arguments.of("psql variables", "SELECT :foo, :'bar', :\"baz\", :{?qux}, $1, 1::integer;", "", Collections.singletonList("SELECT :foo, :'bar', :\"baz\", :{?qux}, $1, 1::integer;")),
                Arguments.of("unknown command in buffer", "SELECT 1\n\\unknown foo\nSELECT 2;", "", Collections.singletonList("SELECT 1\n\\unknown foo\nSELECT 2;")),
                Arguments.of("mixed commands in buffer", "SELECT 1 \\g \\echo foo;\nSELECT 2;", "", Arrays.asList("SELECT 1 \\g \\echo foo;", "SELECT 2;")),
                Arguments.of("unterminated sql", "SELECT\n  1", "", Collections.singletonList("SELECT\n  1")),
                Arguments.of("unclosed parenthesis at eof", "SELECT (1;\nSELECT 2;", "", Collections.singletonList("SELECT (1;\nSELECT 2;")),
                Arguments.of("unclosed atomic body at eof", "CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT 1;\nSELECT 2;", "",
                        Collections.singletonList("CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT 1;\nSELECT 2;")),
                Arguments.of("unknown lexical character", "SELECT 1 " + (char) 1 + "; SELECT 2;", "", Arrays.asList("SELECT 1 " + (char) 1 + ";", "SELECT 2;")),
                Arguments.of("unknown leading lexical character", (char) 127 + "SELECT 1;\nSELECT 2;", "", Arrays.asList((char) 127 + "SELECT 1;", "SELECT 2;")),
                Arguments.of("unicode offsets", "SELECT 'foo😀bar;'; SELECT 2;", "", Arrays.asList("SELECT 'foo😀bar;';", "SELECT 2;")),
                Arguments.of("literal brace and dollar", "SELECT '{foo', '}bar', '$$;'; SELECT 2;", "", Arrays.asList("SELECT '{foo', '}bar', '$$;';", "SELECT 2;")));
    }
    
    private static Stream<Arguments> expectedCases() {
        return Stream.of(
                Arguments.of("blank is not echo anchor", "SELECT 1;\nSELECT 2;", "\nSELECT 1;\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("notice before error", "DO $x$\nBEGIN\n  RAISE NOTICE 'foo';\n  RAISE EXCEPTION 'bar';\nEND $x$;\nSELECT 2;",
                        "DO $x$\nBEGIN\n  RAISE NOTICE 'foo';\n  RAISE EXCEPTION 'bar';\nEND $x$;\nNOTICE: foo\nERROR: bar\nSELECT 2;",
                        Arrays.asList("DO $x$\nBEGIN\n  RAISE NOTICE 'foo';\n  RAISE EXCEPTION 'bar';\nEND $x$;", "SELECT 2;")),
                Arguments.of("notice continuation with error text", "DO $$ BEGIN RAISE NOTICE E'foo\\nERROR: fake'; END $$;\nSELECT 2;",
                        "DO $$ BEGIN RAISE NOTICE E'foo\\nERROR: fake'; END $$;\nNOTICE: foo\nERROR: fake\nSELECT 2;",
                        Arrays.asList("DO $$ BEGIN RAISE NOTICE E'foo\\nERROR: fake'; END $$;", "SELECT 2;")),
                Arguments.of("warning continuation with error text", "SELECT 1;\nSELECT 2;", "SELECT 1;\nWARNING: foo\nERROR: fake\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("info continuation with error text", "SELECT 1;\nSELECT 2;", "SELECT 1;\nINFO: foo\nERROR: fake\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("log continuation with error text", "SELECT 1;\nSELECT 2;", "SELECT 1;\nLOG: foo\nERROR: fake\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("debug continuation with error text", "SELECT 1;\nSELECT 2;", "SELECT 1;\nDEBUG: foo\nERROR: fake\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("multiline echo", "SELECT\n\n  -- foo\n  1;\nSELECT 2;", "SELECT\n  -- foo\n  1;\nERROR: bar\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("space only line in echo", "SELECT\n  \n  1;\nSELECT 2;", "SELECT\n  \n  1;\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("blank line in string echo", "SELECT 'foo\n\nbar';\nSELECT 2;", "SELECT 'foo\n\nbar';\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("blank line in identifier echo", "SELECT \"foo\n\nbar\";\nSELECT 2;", "SELECT \"foo\n\nbar\";\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("blank line in comment echo", "SELECT /*foo\n\nbar*/ 1;\nSELECT 2;", "SELECT /*foo\n\nbar*/ 1;\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("blank line in dollar echo", "SELECT $x$foo\n\nbar$x$;\nSELECT 2;", "SELECT $x$foo\n\nbar$x$;\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("error in output literal", "SELECT 'ERROR' AS foo;\nSELECT 2;", "SELECT 'ERROR' AS foo;\n foo\n-----\n ERROR\n(1 row)\nSELECT 2;",
                        Arrays.asList("SELECT 'ERROR' AS foo;", "SELECT 2;")),
                Arguments.of("error in echoed string", "SELECT 'foo\nERROR: bar\nbaz';\nSELECT 2;", "SELECT 'foo\nERROR: bar\nbaz';\n foo\nSELECT 2;",
                        Arrays.asList("SELECT 'foo\nERROR: bar\nbaz';", "SELECT 2;")),
                Arguments.of("copy output error literal", "COPY (SELECT 'ERROR: client text') TO STDOUT;\nSELECT 2;",
                        "COPY (SELECT 'ERROR: client text') TO STDOUT;\nERROR: client text\nSELECT 2;", Arrays.asList("COPY (SELECT 'ERROR: client text') TO STDOUT;", "SELECT 2;")),
                Arguments.of("error in leading block comment", "SELECT 1;\n/*\nERROR: literal\n*/\nSELECT 2;", "SELECT 1;\n/*\nERROR: literal\n*/\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("unresolved variables with error", "SELECT :foo, :'foo', :\"foo\", :{?foo};\nSELECT 2;", "SELECT :foo, :'foo', :\"foo\", :{?foo};\nERROR: bar\nSELECT 2;",
                        Arrays.asList("SELECT :foo, :'foo', :\"foo\", :{?foo};", "SELECT 2;")),
                Arguments.of("cast and parameter native error", "SELECT $1::integer;\nSELECT 2;", "SELECT $1::integer;\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("array slice in rule action", "CREATE RULE foo AS ON INSERT TO bar DO ALSO (SELECT a[1:2] FROM baz; SELECT 2);\nSELECT 3;",
                        "CREATE RULE foo AS ON INSERT TO bar DO ALSO (SELECT a[1:2] FROM baz; SELECT 2);\nERROR: foo\nSELECT 3;",
                        Arrays.asList("CREATE RULE foo AS ON INSERT TO bar DO ALSO (SELECT a[1:2] FROM baz; SELECT 2);", "SELECT 3;")),
                Arguments.of("variable in atomic body", "CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT :foo; SELECT 2; END;\nSELECT 3;",
                        "CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT :foo; SELECT 2; END;\nERROR: foo\nSELECT 3;",
                        Arrays.asList("CREATE FUNCTION foo() RETURNS integer BEGIN ATOMIC SELECT :foo; SELECT 2; END;", "SELECT 3;")),
                Arguments.of("missing echo", "SELECT 1;\nSELECT 2;", "ERROR: foo\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("reversed echo order", "SELECT 1;\nSELECT 2;", "SELECT 2;\nERROR: foo\nSELECT 1;\nERROR: bar", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("incomplete multiline echo", "SELECT\n  1;\nSELECT 2;", "SELECT", Arrays.asList("SELECT\n  1;", "SELECT 2;")),
                Arguments.of("different multiline echo", "SELECT\n  1;\nSELECT 2;", "SELECT\n  2;\nERROR: foo\nSELECT 2;", Arrays.asList("SELECT\n  1;", "SELECT 2;")),
                Arguments.of("missing next echo", "SELECT 1;\nSELECT 2;", "SELECT 1;\nERROR: foo", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("echo prefix mismatch", "SELECT 12;\nSELECT 2;", "SELECT 1;\nERROR: foo\nSELECT 2;", Arrays.asList("SELECT 12;", "SELECT 2;")),
                Arguments.of("missing duplicate echo", "SELECT 1;\nSELECT 1;", "SELECT 1;\nERROR: foo", Arrays.asList("SELECT 1;", "SELECT 1;")),
                Arguments.of("shared echo line", "SELECT 1; SELECT 2;", "SELECT 1; SELECT 2;\nERROR: foo", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("repeated echo with context", "SELECT 1;\nSELECT 2;\nSELECT 1;", "SELECT 1;\n foo\nSELECT 2;\n bar\nSELECT 1;\nERROR: baz", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("repeated echo after rejected position", "SELECT 1;\nSELECT 2;\nSELECT 1;\nSELECT 1;", "SELECT 1;\nSELECT 1;\nSELECT 2;\nSELECT 1;\nERROR: foo",
                        Arrays.asList("SELECT 1;", "SELECT 2;", "SELECT 1;")),
                Arguments.of("command response boundary", "SELECT 1;\n\\echo foo\nSELECT 2;", "SELECT 1;\n\\echo foo\nERROR: client text\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("empty query buffer submission", "SELECT 1;\n\\g\nSELECT 2;", "SELECT 1;\n\\g\nERROR: client text\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("echo errors mode", "\\set ECHO errors\nSELECT 1;\n\\set ECHO all\nSELECT 2;", "\\set ECHO errors\nERROR: foo\nSTATEMENT: SELECT 1;\nSELECT 2;",
                        Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("unaligned format after echo all", "\\pset format unaligned\n\\set ECHO all\nSELECT 'ERROR: client text';",
                        "\\pset format unaligned\nOutput format is unaligned.\n\\set ECHO all\nSELECT 'ERROR: client text';\n?column?\nERROR: client text\n(1 row)",
                        Collections.singletonList("SELECT 'ERROR: client text';")),
                Arguments.of("printed query buffer", "SELECT 1;\n\\p\nSELECT 2;", "SELECT 1;\n foo\n\\p\nSELECT 1;\nERROR: client text\nSELECT 2;", Arrays.asList("SELECT 1;", "SELECT 2;")),
                Arguments.of("expanded field with error text", "SELECT 1 AS \"ERROR:\" \\gx\nSELECT 2;", "SELECT 1 AS \"ERROR:\" \\gx\n-[ RECORD 1 ]-\nERROR: | 1\nSELECT 2;",
                        Arrays.asList("SELECT 1 AS \"ERROR:\"", "SELECT 2;")),
                Arguments.of("unaligned output with error text", "SELECT 'ERROR: literal' \\g (format=unaligned)\nSELECT 2;",
                        "SELECT 'ERROR: literal' \\g (format=unaligned)\n?column?\nERROR: literal\n(1 row)\nSELECT 2;", Arrays.asList("SELECT 'ERROR: literal'", "SELECT 2;")),
                Arguments.of("g native error", "SELECT 1 \\g\nSELECT 2;", "SELECT 1 \\g\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("gset native error", "SELECT 1 \\gset\nSELECT 2;", "SELECT 1 \\gset\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("multiline g native error", "SELECT\n  1\n\\g\nSELECT 2;", "SELECT\n  1\n\\g\nERROR: foo\nSELECT 2;", Collections.singletonList("SELECT 2;")),
                Arguments.of("gexec generated query", "SELECT 'SELECT 2;' \\gexec\nSELECT 3;", "SELECT 'SELECT 2;' \\gexec\nSELECT 2;\nERROR: foo\nSELECT 3;",
                        Arrays.asList("SELECT 'SELECT 2;'", "SELECT 3;")),
                Arguments.of("copy error without payload echo", "COPY foo FROM STDIN;\nfoo';\n$x$;\n\\.\nSELECT 2;", "COPY foo FROM STDIN;\nERROR: bar\nCONTEXT: COPY foo\nSELECT 2;",
                        Collections.singletonList("SELECT 2;")));
    }
    
    @Test
    void assertLoadNativeError() {
        Collection<ExternalSQLTestParameter> actual = template.load("sample", Collections.singletonList("SELECT 1;"), Arrays.asList("SELECT 1;", "ERROR: foo"), "PostgreSQL", "CSV");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertLoadEmptySource() {
        Collection<ExternalSQLTestParameter> actual = template.load("sample", Collections.emptyList(), Collections.emptyList(), "PostgreSQL", "CSV");
        assertTrue(actual.isEmpty());
    }
}
