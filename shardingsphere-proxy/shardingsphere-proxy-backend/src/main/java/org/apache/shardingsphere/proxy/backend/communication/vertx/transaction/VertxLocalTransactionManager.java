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

package org.apache.shardingsphere.proxy.backend.communication.vertx.transaction;

import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.communication.TransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.vertx.VertxBackendConnection;

/**
 * Vert.x local transaction manager.
 */
@RequiredArgsConstructor
public final class VertxLocalTransactionManager implements TransactionManager<Future<Void>> {
    
    private final VertxBackendConnection connection;
    
    @Override
    public Future<Void> begin() {
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return Future.succeededFuture();
        }
        connection.getConnectionSession().getTransactionStatus().setInTransaction(true);
        connection.getConnectionPostProcessors().add(target -> target.compose(connection -> connection.query("begin").execute().compose(unused -> target)));
        return Future.succeededFuture();
    }
    
    @Override
    public Future<Void> commit() {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return Future.succeededFuture();
        }
        connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
        connection.getConnectionPostProcessors().clear();
        return connection.executeInAllCachedConnections("commit");
    }
    
    @Override
    public Future<Void> rollback() {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return Future.succeededFuture();
        }
        connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
        connection.getConnectionPostProcessors().clear();
        return connection.executeInAllCachedConnections("rollback");
    }
    
    @Override
    public Future<Void> setSavepoint(final String savepointName) {
        return Future.failedFuture(new UnsupportedOperationException());
    }
    
    @Override
    public Future<Void> rollbackTo(final String savepointName) {
        return Future.failedFuture(new UnsupportedOperationException());
    }
    
    @Override
    public Future<Void> releaseSavepoint(final String savepointName) {
        return Future.failedFuture(new UnsupportedOperationException());
    }
}
