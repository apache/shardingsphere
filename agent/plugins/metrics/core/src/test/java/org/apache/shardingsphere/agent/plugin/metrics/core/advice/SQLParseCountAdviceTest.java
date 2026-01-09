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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationListStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.resource.ShowStorageUnitsStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.ParseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SQLParseCountAdviceTest {
    
    private final MetricConfiguration config = new MetricConfiguration("parsed_sql_total", MetricCollectorType.COUNTER, null, Collections.singletonList("type"), Collections.emptyMap());
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @ParameterizedTest(name = "{index}: type={0}")
    @MethodSource("sqlStatements")
    void assertParseSQL(final String type, final SQLStatement sqlStatement, final String expected) {
        new SQLParseCountAdvice().afterMethod(new TargetAdviceObjectFixture(), mock(TargetAdviceMethod.class), new Object[]{}, sqlStatement, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(config, "FIXTURE").toString(), is(expected));
    }
    
    private static Stream<Arguments> sqlStatements() {
        return Stream.of(
                Arguments.of("INSERT", mock(InsertStatement.class), "INSERT=1"),
                Arguments.of("UPDATE", mock(UpdateStatement.class), "UPDATE=1"),
                Arguments.of("DELETE", mock(DeleteStatement.class), "DELETE=1"),
                Arguments.of("SELECT", mock(SelectStatement.class), "SELECT=1"),
                Arguments.of("DDL", mock(CreateDatabaseStatement.class), "DDL=1"),
                Arguments.of("DCL", mock(CreateUserStatement.class), "DCL=1"),
                Arguments.of("DAL", mock(MySQLShowDatabasesStatement.class), "DAL=1"),
                Arguments.of("TCL", mock(CommitStatement.class), "TCL=1"),
                Arguments.of("RQL", new ShowStorageUnitsStatement(new FromDatabaseSegment(0, new DatabaseSegment(0, 0, null)), null), "RQL=1"),
                Arguments.of("RDL", new RegisterStorageUnitStatement(false, Collections.emptyList(), Collections.emptySet()), "RDL=1"),
                Arguments.of("RAL", new ShowMigrationListStatement(), "RAL=1"),
                Arguments.of("RUL", new ParseStatement("SELECT * FROM tbl"), "RUL=1"));
    }
}
