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

package org.apache.shardingsphere.transaction.base.seata.at;

import org.apache.seata.core.context.RootContext;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SeataTransactionalSQLExecutionHookTest {
    
    private final SeataTransactionalSQLExecutionHook executionHook = new SeataTransactionalSQLExecutionHook();
    
    @Mock
    private ConnectionProperties connectionProps;
    
    @AfterEach
    void tearDown() {
        RootContext.unbind();
    }
    
    @Test
    void assertTrunkThreadExecute() {
        RootContext.bind("xid");
        executionHook.start("ds", "SELECT 1", Collections.emptyList(), connectionProps, true);
        assertThat(SeataXIDContext.get(), is(RootContext.getXID()));
        executionHook.finishSuccess();
        assertTrue(RootContext.inGlobalTransaction());
    }
    
    @Test
    void assertChildThreadExecute() {
        executionHook.start("ds", "SELECT 1", Collections.emptyList(), connectionProps, false);
        assertTrue(RootContext.inGlobalTransaction());
        executionHook.finishSuccess();
        assertFalse(RootContext.inGlobalTransaction());
    }
    
    @Test
    void assertChildThreadExecuteFailed() {
        executionHook.start("ds", "SELECT 1", Collections.emptyList(), connectionProps, false);
        assertTrue(RootContext.inGlobalTransaction());
        executionHook.finishFailure(new RuntimeException(""));
        assertFalse(RootContext.inGlobalTransaction());
    }
}
