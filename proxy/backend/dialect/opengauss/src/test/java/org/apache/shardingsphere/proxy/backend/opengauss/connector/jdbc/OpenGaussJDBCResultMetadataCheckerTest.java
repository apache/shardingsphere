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

package org.apache.shardingsphere.proxy.backend.opengauss.connector.jdbc;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.DialectJDBCResultMetadataChecker;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussJDBCResultMetadataCheckerTest {
    
    private final DialectJDBCResultMetadataChecker checker = DatabaseTypedSPILoader.getService(
            DialectJDBCResultMetadataChecker.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"));
    
    @Test
    void assertCheckCompositeTypeAcrossStorageUnits() throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnType(1)).thenReturn(Types.STRUCT);
        when(statement.getMetaData()).thenReturn(metaData);
        Collection<ExecutionUnit> executionUnits = Arrays.asList(
                new ExecutionUnit("ds_0", new SQLUnit("SELECT record_value", Collections.emptyList())),
                new ExecutionUnit("ds_1", new SQLUnit("SELECT record_value", Collections.emptyList())));
        UnsupportedSQLOperationException actual =
                assertThrows(UnsupportedSQLOperationException.class, () -> checker.check(executionUnits, statement, "SELECT record_value"));
        assertThat(actual.getMessage(), is(
                "Unsupported SQL operation: Composite result columns cannot be returned because the query is routed to multiple storage units."));
    }
}
