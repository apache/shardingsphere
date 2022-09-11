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
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.proxy.frontend.exception.CircuitBreakException;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public final class MySQLErrPacketFactoryTest {
    
    @Test
    public void assertNewInstanceWithSQLExceptionForNullSQLState() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new SQLException(""));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1815));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), startsWith("Internal error"));
    }
    
    @Test
    public void assertNewInstanceWithSQLException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new SQLException("No reason", "XXX", 30000, new RuntimeException("")));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(30000));
        assertThat(actual.getSqlState(), is("XXX"));
        assertThat(actual.getErrorMessage(), is("No reason"));
    }
    
    @Test
    public void assertNewInstanceWithShardingSphereSQLException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new CircuitBreakException());
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(10310));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_WARNING.getValue()));
        assertThat(actual.getErrorMessage(), is("Circuit break open, the request has been ignored"));
    }
    
    @Test
    public void assertNewInstanceWithSQLDialectException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new UnknownDatabaseException("foo_db"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(1049));
        assertThat(actual.getSqlState(), is(XOpenSQLState.SYNTAX_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), is("Unknown database 'foo_db'"));
    }
    
    @Test
    public void assertNewInstanceWithUnknownException() {
        MySQLErrPacket actual = MySQLErrPacketFactory.newInstance(new RuntimeException("No reason"));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(30000));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), is("Unknown exception: No reason"));
    }
}
