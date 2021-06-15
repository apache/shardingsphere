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

import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class AlterViewStatementHandlerTest {
    
    @Test
    public void assertGetSelectStatementWithSelectStatementForMySQL() {
        MySQLAlterViewStatement createViewStatement = new MySQLAlterViewStatement();
        createViewStatement.setSelect(new MySQLSelectStatement());
        Optional<SelectStatement> selectStatement = AlterViewStatementHandler.getSelectStatement(createViewStatement);
        assertTrue(selectStatement.isPresent());
    }
    
    @Test
    public void assertGetSelectStatementWithoutSelectStatementForMySQL() {
        MySQLAlterViewStatement createViewStatement = new MySQLAlterViewStatement();
        Optional<SelectStatement> selectStatement = AlterViewStatementHandler.getSelectStatement(createViewStatement);
        assertFalse(selectStatement.isPresent());
    }
}
