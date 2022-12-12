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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dal;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dal.OpenGaussExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dal.PostgreSQLExplainStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dal.SQLServerExplainStatement;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ExplainStatementHandlerTest {
    
    @Test
    public void assertGetSimpleTableSegmentWithSimpleTableSegmentForMySQL() {
        MySQLExplainStatement explainStatement = new MySQLExplainStatement();
        explainStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        Optional<SimpleTableSegment> simpleTableSegment = ExplainStatementHandler.getSimpleTableSegment(explainStatement);
        assertTrue(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentWithoutSimpleTableSegmentForMySQL() {
        MySQLExplainStatement explainStatement = new MySQLExplainStatement();
        Optional<SimpleTableSegment> simpleTableSegment = ExplainStatementHandler.getSimpleTableSegment(explainStatement);
        assertFalse(simpleTableSegment.isPresent());
    }
    
    @Test
    public void assertGetSimpleTableSegmentForOtherDatabases() {
        assertFalse(ExplainStatementHandler.getSimpleTableSegment(new OpenGaussExplainStatement()).isPresent());
        assertFalse(ExplainStatementHandler.getSimpleTableSegment(new PostgreSQLExplainStatement()).isPresent());
        assertFalse(ExplainStatementHandler.getSimpleTableSegment(new SQLServerExplainStatement()).isPresent());
    }
}
