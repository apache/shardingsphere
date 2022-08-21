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

package org.apache.shardingsphere.proxy.frontend.mysql.err;

import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.frontend.exception.CircuitBreakException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedCommandException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySQLErrPacketFactoryTest {
    
    @Test
    public void assertNewInstanceWithSQLException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new SQLException("No reason", "XXX", 30000, new RuntimeException("")));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(30000));
        assertThat(actual.getSqlState(), is("XXX"));
        assertThat(actual.getErrorMessage(), is("No reason"));
    }
    
    @Test
    public void assertNewInstanceWithSQLExceptionOfNullSQLState() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new SQLException(new RuntimeException("No reason")));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1815));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), endsWith("No reason"));
    }
    
    @Test
    public void assertNewInstanceWithSQLExceptionOfNullParam() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new SQLException(""));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1815));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), startsWith("Internal error"));
    }
    
    @Test
    public void assertNewInstanceWithUnsupportedVariableExceptionException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new UnsupportedVariableException("test"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(11001));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), is("Could not support variable `test`"));
    }
    
    @Test
    public void assertNewInstanceWithTableModifyInTransactionException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new TableModifyInTransactionException("unknown_table"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(3176));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), is("Please do not modify the unknown_table table with an XA transaction. "
                + "This is an internal system table used to store GTIDs for committed transactions. "
                + "Although modifying it can lead to an inconsistent GTID state, if necessary you can modify it with a non-XA transaction."));
    }
    
    @Test
    public void assertNewInstanceWithUnknownDatabaseException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new UnknownDatabaseException("ds"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1049));
        assertThat(actual.getSqlState(), is("42000"));
        assertThat(actual.getErrorMessage(), is("Unknown database 'ds'"));
    }
    
    @Test
    public void assertNewInstanceWithNoDatabaseSelectedException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new NoDatabaseSelectedException());
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1046));
        assertThat(actual.getSqlState(), is("3D000"));
        assertThat(actual.getErrorMessage(), is("No database selected"));
    }
    
    @Test
    public void assertNewInstanceWithDBCreateExistsException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new DatabaseCreateExistsException("No reason"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1007));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), is("Can't create database 'No reason'; database exists"));
    }
    
    @Test
    public void assertNewInstanceWithDBDropExistsException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new DatabaseDropNotExistsException("No reason"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1008));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), is("Can't drop database 'No reason'; database doesn't exist"));
    }
    
    @Test
    public void assertNewInstanceWithTableExistsException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new TableExistsException("table_name"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1050));
        assertThat(actual.getSqlState(), is("42S01"));
        assertThat(actual.getErrorMessage(), is("Table 'table_name' already exists"));
    }
    
    @Test
    public void assertNewInstanceWithNoSuchTableException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new NoSuchTableException("table_name"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1146));
        assertThat(actual.getSqlState(), is("42S02"));
        assertThat(actual.getErrorMessage(), is("Table 'table_name' doesn't exist"));
    }
    
    @Test
    public void assertNewInstanceWithShardingSphereConfigurationException() {
        assertShardingSphereConfigurationException(MySQLErrPacketFactory.newInstance(new ShardingSphereConfigurationException("No reason")));
    }
    
    private void assertShardingSphereConfigurationException(final MySQLErrPacket actual) {
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(11001));
        assertThat(actual.getSqlState(), is("42000"));
        assertThat(actual.getErrorMessage(), is("No reason"));
    }
    
    @Test
    public void assertNewInstanceWithSQLParsingException() {
        assertSQLParsingException(MySQLErrPacketFactory.newInstance(new SQLParsingException("test")));
    }
    
    private void assertSQLParsingException(final MySQLErrPacket actual) {
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(11000));
        assertThat(actual.getSqlState(), is("42000"));
        assertThat(actual.getErrorMessage(), is("You have an error in your SQL syntax: test"));
    }
    
    @Test
    public void assertNewInstanceWithUnsupportedPreparedStatementException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new UnsupportedPreparedStatementException());
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1295));
        assertThat(actual.getSqlState(), is("HY000"));
        assertThat(actual.getErrorMessage(), is("This command is not supported in the prepared statement protocol yet"));
    }
    
    @Test
    public void assertNewInstanceWithCircuitBreakException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new CircuitBreakException());
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(10000));
        assertThat(actual.getSqlState(), is("01000"));
        assertThat(actual.getErrorMessage(), is("Circuit break open, the request has been ignored"));
    }
    
    @Test
    public void assertNewInstanceWithRuleDefinitionViolationException() {
        RuleDefinitionViolationException exception = mock(RuleDefinitionViolationException.class);
        when(exception.getErrorCode()).thenReturn(1);
        when(exception.getSQLState()).thenReturn("C0000");
        when(exception.getMessage()).thenReturn("Test error");
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(exception);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1));
        assertThat(actual.getSqlState(), is("C0000"));
        assertThat(actual.getErrorMessage(), is("Test error"));
    }
    
    @Test
    public void assertNewInstanceWithUnsupportedCommandException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new UnsupportedCommandException("No reason"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(12000));
        assertThat(actual.getSqlState(), is("42000"));
        assertThat(actual.getErrorMessage(), is("Unsupported command: No reason"));
    }
    
    @Test
    public void assertNewInstanceWithOtherException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new ReflectiveOperationException("No reason"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(30000));
        assertThat(actual.getSqlState(), is("42000"));
        assertThat(actual.getErrorMessage(), is("Unknown exception: No reason"));
    }
}
