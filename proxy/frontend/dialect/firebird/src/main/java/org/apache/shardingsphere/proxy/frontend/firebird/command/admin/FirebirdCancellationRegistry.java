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

package org.apache.shardingsphere.proxy.frontend.firebird.command.admin;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.frontend.executor.UserExecutorGroup;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.jdbc.FBConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Cancellation state for Firebird connections and their active commands.
 */
@HighFrequencyInvocation
public final class FirebirdCancellationRegistry {
    
    private static final FirebirdCancellationRegistry INSTANCE = new FirebirdCancellationRegistry();
    
    private final Map<Integer, State> connections = new ConcurrentHashMap<>();
    
    private FirebirdCancellationRegistry() {
    }
    
    /**
     * Get the cancellation registry.
     *
     * @return registry
     */
    @HighFrequencyInvocation
    public static FirebirdCancellationRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Start tracking a command before it is dispatched.
     *
     * @param connectionId connection ID
     */
    @HighFrequencyInvocation
    public void beginCommand(final int connectionId) {
        State state = connections.computeIfAbsent(connectionId, ignored -> new State());
        synchronized (state) {
            state.command = new Command();
        }
    }
    
    /**
     * Finish tracking the dispatched command.
     *
     * @param connectionId connection ID
     */
    @HighFrequencyInvocation
    public void endCommand(final int connectionId) {
        State state = connections.get(connectionId);
        if (null == state) {
            return;
        }
        Command command;
        synchronized (state) {
            command = state.command;
            state.command = null;
        }
        if (null != command) {
            command.finished = true;
        }
    }
    
    /**
     * Apply a cancellation kind without writing a protocol response.
     *
     * @param connectionId connection ID
     * @param kind cancellation kind
     */
    @HighFrequencyInvocation
    public void apply(final int connectionId, final int kind) {
        State state = connections.computeIfAbsent(connectionId, ignored -> new State());
        Command command;
        synchronized (state) {
            if (ISCConstants.fb_cancel_disable == kind) {
                state.enabled = false;
                return;
            }
            if (ISCConstants.fb_cancel_enable == kind) {
                state.enabled = true;
                return;
            }
            if (ISCConstants.fb_cancel_abort != kind && (ISCConstants.fb_cancel_raise != kind || !state.enabled)) {
                return;
            }
            command = state.command;
        }
        if (null == command) {
            return;
        }
        if (command.finished) {
            return;
        }
        if (ISCConstants.fb_cancel_abort == kind) {
            command.aborted = true;
        }
        command.cancelled = true;
        if (null != command.manager && command.cancellationRunning.compareAndSet(false, true)) {
            UserExecutorGroup.getInstance().getExecutorService().execute(() -> cancel(command));
        }
    }
    
    @HighFrequencyInvocation
    private void cancel(final Command command) {
        try {
            while (true) {
                synchronized (command) {
                    if (command.finished || null == command.manager) {
                        return;
                    }
                    Collection<Connection> connections;
                    synchronized (command.manager.getCachedConnections()) {
                        connections = new ArrayList<>(command.manager.getCachedConnections().values());
                    }
                    int kind = command.aborted ? ISCConstants.fb_cancel_abort : ISCConstants.fb_cancel_raise;
                    cancelConnections(connections, kind);
                }
                LockSupport.parkNanos(50_000_000L);
            }
        } finally {
            command.cancellationRunning.set(false);
        }
    }
    
    private void cancelConnections(final Collection<Connection> connections, final int kind) {
        for (Connection each : connections) {
            try {
                FBConnection connection = each.unwrap(FBConnection.class);
                if (ISCConstants.fb_cancel_abort == kind) {
                    byte[] packet = ByteBuffer.allocate(8).putInt(FirebirdCommandPacketType.CANCEL.getValue()).putInt(kind).array();
                    ((FbWireDatabase) connection.getFbDatabase()).getWireOperations().writeDirect(packet);
                } else {
                    connection.getFbDatabase().cancelOperation(kind);
                }
            } catch (final IOException | SQLException ignored) {
            }
        }
    }
    
    /**
     * Bind the active command to its transaction's backend connections.
     *
     * @param connectionId connection ID
     * @param manager transaction's connection manager
     * @return scope that must be closed before releasing connections
     * @throws SQLException if cancellation preceded backend execution
     */
    @HighFrequencyInvocation
    public Scope enter(final int connectionId, final ProxyDatabaseConnectionManager manager) throws SQLException {
        State state = connections.get(connectionId);
        if (null == state) {
            return new Scope();
        }
        Command command;
        synchronized (state) {
            command = state.command;
        }
        if (null == command) {
            return new Scope();
        }
        synchronized (command) {
            if (command.finished) {
                return new Scope();
            }
            if (command.cancelled) {
                throw new SQLException("operation was cancelled", "HY008", ISCConstants.isc_cancelled);
            }
            command.manager = manager;
            if (command.cancelled) {
                command.manager = null;
                throw new SQLException("operation was cancelled", "HY008", ISCConstants.isc_cancelled);
            }
        }
        return new Scope(command);
    }
    
    /**
     * Forget the connection and any pending cancellation.
     *
     * @param connectionId connection ID
     */
    @HighFrequencyInvocation
    public void unregister(final int connectionId) {
        endCommand(connectionId);
        connections.remove(connectionId);
    }
    
    /**
     * Lifetime of the backend work for a command.
     */
    public static final class Scope implements AutoCloseable {
        
        private final Command command;
        
        @HighFrequencyInvocation
        private Scope() {
            command = null;
        }
        
        @HighFrequencyInvocation
        private Scope(final Command command) {
            this.command = command;
        }
        
        @Override
        @HighFrequencyInvocation
        public void close() {
            if (null != command) {
                synchronized (command) {
                    command.manager = null;
                }
            }
        }
    }
    
    private static final class State {
        
        private boolean enabled = true;
        
        private Command command;
    }
    
    private static final class Command {
        
        private volatile ProxyDatabaseConnectionManager manager;
        
        private volatile boolean cancelled;
        
        private volatile boolean aborted;
        
        private final AtomicBoolean cancellationRunning = new AtomicBoolean();
        
        private volatile boolean finished;
    }
}
