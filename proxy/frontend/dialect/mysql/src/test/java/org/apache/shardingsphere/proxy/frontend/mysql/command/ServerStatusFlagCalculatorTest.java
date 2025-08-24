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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerStatusFlagCalculatorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertAutoCommitNotInTransaction() {
        when(connectionSession.isAutoCommit()).thenReturn(true);
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession, true), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
    
    @Test
    void assertAutoCommitInTransaction() {
        when(connectionSession.isAutoCommit()).thenReturn(true);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession, true), is(MySQLStatusFlag.SERVER_STATUS_IN_TRANS.getValue() | MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
    
    @Test
    void assertNotAutoCommitNotInTransaction() {
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession, true), is(0));
    }
    
    @Test
    void assertNotAutoCommitInTransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession, true), is(MySQLStatusFlag.SERVER_STATUS_IN_TRANS.getValue()));
    }
    
    @Test
    void assertCalculateForWithMultiStatements() {
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession, false), is(MySQLStatusFlag.SERVER_MORE_RESULTS_EXISTS.getValue()));
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession, true), is(0));
    }
}
