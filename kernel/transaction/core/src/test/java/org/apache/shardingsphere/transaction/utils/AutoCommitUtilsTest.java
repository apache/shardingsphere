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

package org.apache.shardingsphere.transaction.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

public final class AutoCommitUtilsTest {
    
    @Test
    public void assertNeedOpenTransactionForSelectStatement() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        assertFalse(AutoCommitUtils.needOpenTransaction(selectStatement));
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo"))));
        assertTrue(AutoCommitUtils.needOpenTransaction(selectStatement));
    }
    
    @Test
    public void assertNeedOpenTransactionForDDLOrDMLStatement() {
        assertTrue(AutoCommitUtils.needOpenTransaction(new MySQLCreateTableStatement(true)));
        assertTrue(AutoCommitUtils.needOpenTransaction(new MySQLInsertStatement()));
    }
    
    @Test
    public void assertNeedOpenTransactionForOtherStatement() {
        assertFalse(AutoCommitUtils.needOpenTransaction(new RegisterStorageUnitStatement(new LinkedList<>())));
    }
}
