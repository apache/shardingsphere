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

package org.apache.shardingsphere.agent.plugin.core.util;

import org.apache.shardingsphere.agent.plugin.core.enums.SQLStatementType;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.RALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class SQLStatementUtilsTest {
    
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideStatements")
    void assertGetType(final String name, final SQLStatement sqlStatement, final SQLStatementType expectedType) {
        assertThat(SQLStatementUtils.getType(sqlStatement), is(expectedType));
    }
    
    private static Stream<Arguments> provideStatements() {
        return Stream.of(
                Arguments.of("null", null, SQLStatementType.OTHER),
                Arguments.of("select", mock(SelectStatement.class), SQLStatementType.SELECT),
                Arguments.of("insert", mock(InsertStatement.class), SQLStatementType.INSERT),
                Arguments.of("update", mock(UpdateStatement.class), SQLStatementType.UPDATE),
                Arguments.of("delete", mock(DeleteStatement.class), SQLStatementType.DELETE),
                Arguments.of("dml-other", mock(DMLStatement.class), SQLStatementType.DML),
                Arguments.of("ddl", mock(DDLStatement.class), SQLStatementType.DDL),
                Arguments.of("dcl", mock(DCLStatement.class), SQLStatementType.DCL),
                Arguments.of("dal", mock(DALStatement.class), SQLStatementType.DAL),
                Arguments.of("tcl", mock(TCLStatement.class), SQLStatementType.TCL),
                Arguments.of("rql", mock(RQLStatement.class), SQLStatementType.RQL),
                Arguments.of("rdl", mock(RDLStatement.class), SQLStatementType.RDL),
                Arguments.of("ral", mock(RALStatement.class), SQLStatementType.RAL),
                Arguments.of("rul", mock(RULStatement.class), SQLStatementType.RUL),
                Arguments.of("distsql-other", mock(DistSQLStatement.class), SQLStatementType.OTHER),
                Arguments.of("other", mock(SQLStatement.class), SQLStatementType.OTHER)
        );
    }
}
