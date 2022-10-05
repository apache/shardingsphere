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

package org.apache.shardingsphere.proxy.frontend.reactive.wrap;

import io.vertx.core.Future;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class WrappedReactiveCommandExecutorTest {
    
    @Mock
    private CommandExecutor commandExecutor;
    
    @InjectMocks
    private WrappedReactiveCommandExecutor executor;
    
    @Test
    public void assertExecuteFutureSucceeded() throws SQLException {
        DatabasePacket<?> expectedPacket = mock(DatabasePacket.class);
        List<DatabasePacket<?>> expected = Collections.singletonList(expectedPacket);
        when(commandExecutor.execute()).thenReturn(expected);
        Future<Collection<DatabasePacket<?>>> actualFuture = executor.executeFuture();
        assertThat(actualFuture.result(), is(expected));
    }
    
    @Test
    public void assertExecuteFutureFailed() throws SQLException {
        RuntimeException expected = new RuntimeException();
        when(commandExecutor.execute()).thenThrow(expected);
        Future<Collection<DatabasePacket<?>>> actualFuture = executor.executeFuture();
        assertThat(actualFuture.cause(), is(expected));
    }
    
    @Test
    public void assertCloseFutureSucceeded() throws SQLException {
        assertThat(executor.closeFuture(), is(Future.succeededFuture()));
        verify(commandExecutor).close();
    }
    
    @Test
    public void assertCloseFutureFailed() throws SQLException {
        SQLException expected = mock(SQLException.class);
        doThrow(expected).when(commandExecutor).close();
        assertThat(executor.closeFuture().cause(), is(expected));
    }
}
