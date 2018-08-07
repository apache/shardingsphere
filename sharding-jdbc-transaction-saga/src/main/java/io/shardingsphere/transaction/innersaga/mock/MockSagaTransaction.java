/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.innersaga.mock;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Saga transaction mock implement.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public class MockSagaTransaction implements SagaTransaction {
    
    @Getter
    private final String transactionId = UUID.randomUUID().toString();
    
    private final int begin = 1;
    
    private final int start = 2;
    
    private final int close = 3;
    
    private int status = begin;
    
    @NonNull
    private SagaTransactionManager sagaTransactionManager;
    
    private final Map<String, SagaSubTransaction> subMap = new ConcurrentHashMap<>();
    
    private Queue<String> confirmQuere = new LinkedBlockingQueue<>();
    
    private LinkedBlockingDeque<String> cancelStack = new LinkedBlockingDeque<>();
    
    @Override
    public void register(final SagaSubTransaction sagaSubTransaction) {
        synchronized (subMap) {
            subMap.put(sagaSubTransaction.getSubId(), sagaSubTransaction);
        }
    }
    
    @Override
    public void begin() {
        status = start;
    }
    
    @Override
    public void success(final String subId) {
        cancelStack.add(subId);
    }
    
    @Override
    public void fail(final String subId) {
        confirmQuere.add(subId);
    }
    
    @Override
    public void commit() throws Exception {
        synchronized (subMap) {
            for (String subId : confirmQuere) {
                if (subMap.containsKey(subId)) {
                    SagaSubTransaction sagaSubTransaction = subMap.get(subId);
                    try (Connection connection = sagaTransactionManager.getTargetConnection(sagaSubTransaction.getDatasource());
                         PreparedStatement statement = connection.prepareStatement(sagaSubTransaction.getConfirm())) {
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        writeLog();
                        throw e;
                    }
                }
            }
            close();
        }
    }
    
    @Override
    public void rollback() throws Exception {
        synchronized (subMap) {
            while (!cancelStack.isEmpty()) {
                String subId = cancelStack.pollLast();
                if (subMap.containsKey(subId)) {
                    SagaSubTransaction sagaSubTransaction = subMap.get(subId);
                    try (Connection connection = sagaTransactionManager.getTargetConnection(sagaSubTransaction.getDatasource());
                         PreparedStatement statement = connection.prepareStatement(sagaSubTransaction.getCancel())) {
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        writeLog();
                        throw e;
                    }
                }
            }
            close();
        }
    }
    
    @Override
    public int getStatus() {
        return 0;
    }
    
    private void writeLog() throws Exception {
        // write transaction log to log table
    }
    
    private void close() {
        subMap.clear();
        confirmQuere.clear();
        cancelStack.clear();
        status = close;
    }
    
}
