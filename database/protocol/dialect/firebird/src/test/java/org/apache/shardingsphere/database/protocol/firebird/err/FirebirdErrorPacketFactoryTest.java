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

package org.apache.shardingsphere.database.protocol.firebird.err;

import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FirebirdErrorPacketFactoryTest {
    
    @Test
    void assertNewInstanceWithUnknownException() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new RuntimeException("No reason"));
        assertThat(actual.getErrorCode(), is(335544382));
        assertThat(actual.getErrorMessage(), is("Unknown exception." + System.lineSeparator() + "More details: java.lang.RuntimeException: No reason"));
    }
    
    @Test
    void assertNewInstanceWithSQLException() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new SQLException("Table not found", "42000", 335544374));
        assertThat(actual.getErrorCode(), is(335544374));
        assertThat(actual.getErrorMessage(), is("Table not found"));
    }
    
    @Test
    void assertNewInstanceWithUnknownDatabaseException() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new UnknownDatabaseException("logic_db"));
        assertThat(actual.getErrorCode(), is(335544375));
        assertThat(actual.getErrorMessage(), is("logic_db"));
    }
    
    @Test
    void assertNewInstanceWithAccessDeniedException() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new AccessDeniedException("root", "127.0.0.1", true));
        assertThat(actual.getErrorCode(), is(335544472));
        assertThat(actual.getErrorMessage(), is(""));
    }
    
    @Test
    void assertNewInstanceWithBatchTooBigException() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new BatchTooBigException(42, 1L, 8L, 8L));
        assertThat(actual.getErrorCode(), is(335545198));
        assertThat(actual.getErrorMessage(), is(""));
    }
    
    @Test
    void assertNewInstanceWithInvalidBatchHandleException() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new InvalidBatchHandleException(42));
        assertThat(actual.getErrorCode(), is(335545159));
        assertThat(actual.getErrorMessage(), is(""));
    }
    
    @Test
    void assertNewInstanceWithNonFirebirdErrorCode() {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new SQLException("Invalid error", "HY000", 123));
        assertThat(actual.getErrorCode(), is(335544382));
        assertThat(actual.getErrorMessage(), is("Invalid error"));
    }
}
