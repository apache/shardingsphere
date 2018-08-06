package io.shardingsphere.transaction.innersaga.mock;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Saga transaction mock implement
 *
 * @author yangyi
 */
@RequiredArgsConstructor
public class MockSagaTransaction implements SagaTransaction {

    @Getter
    private final String transactionId = UUID.randomUUID().toString();

    private final int BEGIN = 1;
    private final int START = 2;
    private final int CLOSE = 3;

    private int status = BEGIN;

    @NonNull
    private SagaTransactionManager sagaTransactionManager;

    private final Map<String, SagaSubTransaction> subMap = new ConcurrentHashMap<>();

    private Queue<String> confirmQuere = new LinkedBlockingQueue<>();

    private LinkedBlockingDeque<String> cancelStack = new LinkedBlockingDeque<>();

    @Override
    public void register(SagaSubTransaction sagaSubTransaction) {
        synchronized (subMap) {
            subMap.put(sagaSubTransaction.getSubId(), sagaSubTransaction);
        }
    }

    @Override
    public void begin() {
        status = START;
    }

    @Override
    public void success(String subId) {
        cancelStack.add(subId);
    }

    @Override
    public void fail(String subId) {
        confirmQuere.add(subId);
    }

    @Override
    public void commit() throws Exception{
        synchronized (subMap) {
            for (String subId : confirmQuere) {
                if (subMap.containsKey(subId)) {
                    SagaSubTransaction sagaSubTransaction = subMap.get(subId);
                    try (Connection connection = sagaTransactionManager.getTargetConnection(sagaSubTransaction.getDatasource());
                         PreparedStatement statement = connection.prepareStatement(sagaSubTransaction.getConfirm())) {
                        statement.executeUpdate();
                    } catch (Exception e) {
                        writeLog();
                        throw e;
                    }
                }
            }
            close();
        }
    }

    @Override
    public void rollback() throws Exception{
        synchronized (subMap) {
            while (!cancelStack.isEmpty()) {
                String subId = cancelStack.pollLast();
                if (subMap.containsKey(subId)) {
                    SagaSubTransaction sagaSubTransaction = subMap.get(subId);
                    try (Connection connection = sagaTransactionManager.getTargetConnection(sagaSubTransaction.getDatasource());
                         PreparedStatement statement = connection.prepareStatement(sagaSubTransaction.getCancel())) {
                        statement.executeUpdate();
                    } catch (Exception e) {
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
        status = CLOSE;
    }

}
