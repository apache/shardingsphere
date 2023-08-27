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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateIndexStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateIndexStatementHandlerTest {
    
    @Test
    void assertIfNotExists() {
        assertFalse(CreateIndexStatementHandler.ifNotExists(new MySQLCreateIndexStatement()));
        assertTrue(CreateIndexStatementHandler.ifNotExists(new PostgreSQLCreateIndexStatement(true)));
        assertTrue(CreateIndexStatementHandler.ifNotExists(new OpenGaussCreateIndexStatement(true)));
        assertFalse(CreateIndexStatementHandler.ifNotExists(new OracleCreateIndexStatement()));
        assertFalse(CreateIndexStatementHandler.ifNotExists(new SQLServerCreateIndexStatement()));
    }
    
    @Test
    void assertGeneratedIndexStartIndexForPostgreSQL() {
        PostgreSQLCreateIndexStatement createIndexStatement = new PostgreSQLCreateIndexStatement(true);
        createIndexStatement.setGeneratedIndexStartIndex(2);
        Optional<Integer> actual = CreateIndexStatementHandler.getGeneratedIndexStartIndex(createIndexStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(2));
    }
    
    @Test
    void assertGeneratedIndexStartIndexForOpenGauss() {
        OpenGaussCreateIndexStatement createIndexStatement = new OpenGaussCreateIndexStatement(true);
        createIndexStatement.setGeneratedIndexStartIndex(2);
        Optional<Integer> actual = CreateIndexStatementHandler.getGeneratedIndexStartIndex(createIndexStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(2));
    }
    
    @Test
    void assertGeneratedIndexStartIndexForOtherDatabases() {
        assertFalse(CreateIndexStatementHandler.getGeneratedIndexStartIndex(new MySQLCreateIndexStatement()).isPresent());
        assertFalse(CreateIndexStatementHandler.getGeneratedIndexStartIndex(new OracleCreateIndexStatement()).isPresent());
        assertFalse(CreateIndexStatementHandler.getGeneratedIndexStartIndex(new SQLServerCreateIndexStatement()).isPresent());
    }
}
