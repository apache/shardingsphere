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

package org.apache.shardingsphere.distsql.handler.util;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.available.FromDatabaseAvailable;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DatabaseNameUtilsTest {
    
    @Test
    void assertDatabaseNameWhenAvailableInSQLStatement() {
        FromDatabaseAvailable sqlStatement = mock(FromDatabaseAvailable.class, withSettings().extraInterfaces(SQLStatement.class));
        DatabaseSegment databaseSegment = mock(DatabaseSegment.class, RETURNS_DEEP_STUBS);
        when(databaseSegment.getIdentifier().getValue()).thenReturn("bar_db");
        when(sqlStatement.getDatabase()).thenReturn(Optional.of(databaseSegment));
        assertThat(DatabaseNameUtils.getDatabaseName((SQLStatement) sqlStatement, "foo_db"), is("bar_db"));
    }
    
    @Test
    void assertDatabaseNameWhenNotAvailableInSQLStatement() {
        assertThat(DatabaseNameUtils.getDatabaseName(mock(SQLStatement.class), "foo_db"), is("foo_db"));
    }
}
