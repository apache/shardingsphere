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

package org.apache.shardingsphere.infra.optimize.converter.parameterized.engine;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.calcite.config.CalciteConnectionConfig;
import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParser.Config;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.util.Litmus;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.optimize.context.parser.dialect.OptimizerSQLDialectBuilderFactory;
import org.apache.shardingsphere.infra.optimize.converter.SQLNodeConvertEngine;
import org.apache.shardingsphere.infra.optimize.converter.parameterized.jaxb.SQLNodeConvertCasesRegistry;
import org.apache.shardingsphere.infra.optimize.converter.parameterized.loader.SQLNodeConvertCasesLoader;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class SQLNodeConvertParameterizedTest {
    
    private static final SQLNodeConvertCasesLoader SQL_NODE_CONVERT_CASES_LOADER = SQLNodeConvertCasesRegistry.getInstance().getSqlNodeConvertCasesLoader();
    
    private final String caseId;
    
    private final String databaseType;
    
    @Parameters(name = "{0} -> {1}")
    public static Collection<Object[]> getTestParameters() {
        return SQLNodeConvertParameterizedTest.getTestParameters("H2", "MySQL", "PostgreSQL", "Oracle", "SQLServer", "SQL92");
    }
    
    private static Collection<Object[]> getTestParameters(final String... databaseTypes) {
        return SQL_NODE_CONVERT_CASES_LOADER.getTestParameters(Arrays.asList(databaseTypes));
    }
    
    @Test
    public void assertConvertToSQLNode() {
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_NODE_CONVERT_CASES_LOADER.getCaseValue(caseId);
        SqlNode actual = SQLNodeConvertEngine.convertToSQLNode(parseSQLStatement(databaseType, sql));
        SqlNode expected = parseSqlNode(databaseType, sql);
        assertTrue(actual.equalsDeep(expected, Litmus.THROW));
    }
    
    @Test
    public void assertConvertToSQLStatement() {
        String databaseType = "H2".equals(this.databaseType) ? "MySQL" : this.databaseType;
        String sql = SQL_NODE_CONVERT_CASES_LOADER.getCaseValue(caseId);
        SQLStatement actual = SQLNodeConvertEngine.convertToSQLStatement(parseSqlNode(databaseType, sql));
        SQLStatement expected = parseSQLStatement(databaseType, sql);
        assertSelectStatement(actual, expected);
    }
    
    private void assertSelectStatement(final SQLStatement actual, final SQLStatement expected) {
        assertThat(actual, instanceOf(SelectStatement.class));
        assertThat(expected, instanceOf(SelectStatement.class));
        assertProjections(((SelectStatement) actual).getProjections(), ((SelectStatement) expected).getProjections());
    }
    
    private void assertProjections(final ProjectionsSegment actual, final ProjectionsSegment expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertThat(actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(actual.getStopIndex(), is(expected.getStopIndex()));
        assertThat(actual.isDistinctRow(), is(expected.isDistinctRow()));
        assertProjections(actual.getProjections(), expected.getProjections());
    }
    
    private void assertProjections(final Collection<ProjectionSegment> actual, final Collection<ProjectionSegment> expected) {
        assertThat(actual.size(), is(expected.size()));
        List<ProjectionSegment> actualProjections = new ArrayList<>(actual);
        List<ProjectionSegment> expectedProjections = new ArrayList<>(expected);
        for (int index = 0; index < actualProjections.size(); index++) {
            ProjectionSegment actualProjection = actualProjections.get(index);
            ProjectionSegment expectedProjection = expectedProjections.get(index);
            assertProjection(actualProjection, expectedProjection);
        }
    }
    
    private void assertProjection(final ProjectionSegment actual, final ProjectionSegment expected) {
        assertCommon(actual, expected);
    }
    
    private void assertCommon(final ProjectionSegment actual, final ProjectionSegment expected) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertThat(actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(actual.getStopIndex(), is(expected.getStopIndex()));
    }
    
    @SneakyThrows(SqlParseException.class)
    private SqlNode parseSqlNode(final String databaseType, final String sql) {
        return SqlParser.create(sql, createConfig(DatabaseTypeRegistry.getActualDatabaseType(databaseType))).parseQuery();
    }
    
    private Config createConfig(final DatabaseType databaseType) {
        CalciteConnectionConfig connectionConfig = new CalciteConnectionConfigImpl(createSQLDialectProperties(databaseType));
        return SqlParser.config().withLex(connectionConfig.lex())
                .withIdentifierMaxLength(SqlParser.DEFAULT_IDENTIFIER_MAX_LENGTH).withConformance(connectionConfig.conformance()).withParserFactory(SqlParserImpl.FACTORY);
    }
    
    private Properties createSQLDialectProperties(final DatabaseType databaseType) {
        Properties result = new Properties();
        result.setProperty(CalciteConnectionProperty.TIME_ZONE.camelName(), "UTC");
        result.putAll(OptimizerSQLDialectBuilderFactory.build(databaseType, result));
        return result;
    }
    
    private SQLStatement parseSQLStatement(final String databaseType, final String sql) {
        return new SQLVisitorEngine(databaseType, "STATEMENT", new Properties()).visit(new SQLParserEngine(databaseType, true).parse(sql, false));
    }
}
