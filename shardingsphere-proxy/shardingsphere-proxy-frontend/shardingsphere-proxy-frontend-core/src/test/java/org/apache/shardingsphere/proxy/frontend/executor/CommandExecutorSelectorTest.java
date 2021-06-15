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

package org.apache.shardingsphere.proxy.frontend.executor;

import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class CommandExecutorSelectorTest {
    
    @Test
    public void assertGetExecutorServiceWithLocal() {
        int connectionId = 1;
        assertThat(CommandExecutorSelector.getExecutorService(false, false, TransactionType.LOCAL, connectionId), instanceOf(ExecutorService.class));
    }
    
    @Test
    public void assertGetExecutorServiceWithOccupyThreadForPerConnection() {
        int connectionId = 2;
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        assertThat(CommandExecutorSelector.getExecutorService(true, false, TransactionType.LOCAL, connectionId), instanceOf(ExecutorService.class));
    }
    
    @Test
    public void assertGetExecutorServiceWithXA() {
        int connectionId = 3;
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        assertThat(CommandExecutorSelector.getExecutorService(false, false, TransactionType.XA, connectionId), instanceOf(ExecutorService.class));
    }
    
    @Test
    public void assertGetExecutorServiceWithBASE() {
        int connectionId = 4;
        ConnectionThreadExecutorGroup.getInstance().register(connectionId);
        assertThat(CommandExecutorSelector.getExecutorService(false, false, TransactionType.BASE, connectionId), instanceOf(ExecutorService.class));
    }
}
