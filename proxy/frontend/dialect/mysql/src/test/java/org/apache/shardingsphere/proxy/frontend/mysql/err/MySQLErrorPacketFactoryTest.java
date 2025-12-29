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

import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLErrorPacketFactoryTest {
    
    @Test
    void assertNewInstanceWithoutSQLState() {
        MySQLErrPacket actual = MySQLErrorPacketFactory.newInstance(new SQLException("No reason"));
        assertThat(actual.getErrorCode(), is(1815));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), is("Internal error: No reason"));
    }
    
    @Test
    void assertNewInstanceWithSQLState() {
        MySQLErrPacket actual = MySQLErrorPacketFactory.newInstance(new RuntimeException("No reason"));
        assertThat(actual.getErrorCode(), is(30000));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), is("Unknown exception." + System.lineSeparator() + "More details: java.lang.RuntimeException: No reason"));
    }
    
    @Test
    void assertNewInstanceWithoutErrorMessageUsesNextExceptionMessage() {
        SQLException cause = new SQLException("");
        cause.setNextException(new SQLException("Next reason"));
        MySQLErrPacket actual = MySQLErrorPacketFactory.newInstance(cause);
        assertThat(actual.getErrorCode(), is(1815));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), is("Internal error: Next reason"));
    }
    
    @Test
    void assertNewInstanceWithNextExceptionButUsesCurrentMessage() {
        SQLException cause = new SQLException("Primary reason");
        cause.setNextException(new SQLException("Next reason"));
        MySQLErrPacket actual = MySQLErrorPacketFactory.newInstance(cause);
        assertThat(actual.getErrorCode(), is(1815));
        assertThat(actual.getSqlState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getErrorMessage(), is("Internal error: Primary reason"));
    }
}
