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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ServerStatusFlagCalculatorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    public void assertAutoCommitNotInTransaction() {
        when(connectionSession.isAutoCommit()).thenReturn(true);
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
    
    @Test
    public void assertAutoCommitInTransaction() {
        when(connectionSession.isAutoCommit()).thenReturn(true);
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession), is(MySQLStatusFlag.SERVER_STATUS_IN_TRANS.getValue() | MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
    
    @Test
    public void assertNotAutoCommitNotInTransaction() {
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession), is(0));
    }
    
    @Test
    public void assertNotAutoCommitInTransaction() {
        when(connectionSession.getTransactionStatus().isInTransaction()).thenReturn(true);
        assertThat(ServerStatusFlagCalculator.calculateFor(connectionSession), is(MySQLStatusFlag.SERVER_STATUS_IN_TRANS.getValue()));
    }
}
