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

package org.apache.shardingsphere.data.pipeline.core.preparer.datasource;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractDataSourcePreparerTest {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private final AbstractDataSourcePreparer preparer = new AbstractDataSourcePreparer() {
        
        @Override
        public void prepareTargetTables(final PrepareTargetTablesParameter param) {
        }
        
        @Override
        public String getDatabaseType() {
            return "FIXTURE";
        }
    };
    
    @Test
    void assertExecuteTargetTableSQL() throws SQLException {
        Statement statement = mock(Statement.class);
        Connection targetConnection = mock(Connection.class);
        when(targetConnection.createStatement()).thenReturn(statement);
        String sql = "CREATE TABLE t (id int)";
        preparer.executeTargetTableSQL(targetConnection, sql);
        verify(statement).execute(sql);
    }
    
    @Test
    void assertAddIfNotExistsForCreateTableSQL() {
        Collection<String> createTableSQLs = Arrays.asList("CREATE TABLE IF NOT EXISTS t (id int)", "CREATE TABLE t (id int)",
                "CREATE  TABLE IF \nNOT \tEXISTS t (id int)", "CREATE \tTABLE t (id int)", "CREATE TABLE \tt_order (id bigint) WITH (orientation=row, compression=no);");
        for (String each : createTableSQLs) {
            String actual = preparer.addIfNotExistsForCreateTableSQL(each);
            assertTrue(PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(actual).find());
        }
        Collection<String> mismatchedSQLs = Arrays.asList("SET search_path = public", "UPDATE t_order SET id = 1");
        for (String each : mismatchedSQLs) {
            String actual = preparer.addIfNotExistsForCreateTableSQL(each);
            assertThat(actual, is(each));
        }
    }
}
