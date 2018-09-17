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

package io.shardingsphere.transaction.bed.sync;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.event.executor.DMLExecutionEvent;
import io.shardingsphere.transaction.api.SoftTransactionManager;
import io.shardingsphere.transaction.api.config.SoftTransactionConfiguration;
import io.shardingsphere.transaction.bed.BEDSoftTransaction;
import io.shardingsphere.transaction.constants.SoftTransactionType;
import io.shardingsphere.transaction.storage.TransactionLog;
import io.shardingsphere.transaction.storage.TransactionLogStorage;
import io.shardingsphere.transaction.storage.TransactionLogStorageFactory;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Best efforts delivery B.A.S.E transaction listener.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@Slf4j
public final class BestEffortsDeliveryListener {
    
    /**
     * Listen event.
     * 
     * @param event dml execution event
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final DMLExecutionEvent event) {
        if (!isProcessContinuously()) {
            return;
        }
        SoftTransactionConfiguration transactionConfig = SoftTransactionManager.getCurrentTransactionConfiguration().get();
        TransactionLogStorage transactionLogStorage = TransactionLogStorageFactory.createTransactionLogStorage(transactionConfig.buildTransactionLogDataSource());
        BEDSoftTransaction bedSoftTransaction = (BEDSoftTransaction) SoftTransactionManager.getCurrentTransaction().get();
        switch (event.getEventType()) {
            case BEFORE_EXECUTE:
                //TODO for batch SQL need split to 2-level records
                transactionLogStorage.add(new TransactionLog(event.getId(), bedSoftTransaction.getTransactionId(), bedSoftTransaction.getTransactionType(), 
                        event.getRouteUnit().getDataSourceName(), event.getRouteUnit().getSqlUnit().getSql(), event.getParameters(), System.currentTimeMillis(), 0));
                return;
            case EXECUTE_SUCCESS: 
                transactionLogStorage.remove(event.getId());
                return;
            case EXECUTE_FAILURE: 
                boolean deliverySuccess = false;
                for (int i = 0; i < transactionConfig.getSyncMaxDeliveryTryTimes(); i++) {
                    if (deliverySuccess) {
                        return;
                    }
                    boolean isNewConnection = false;
                    Connection conn = null;
                    PreparedStatement preparedStatement = null;
                    try {
                        conn = bedSoftTransaction.getConnection().getConnection(event.getRouteUnit().getDataSourceName());
                        if (!isValidConnection(conn)) {
                            bedSoftTransaction.getConnection().release(conn);
                            conn = bedSoftTransaction.getConnection().getConnection(event.getRouteUnit().getDataSourceName());
                            isNewConnection = true;
                        }
                        preparedStatement = conn.prepareStatement(event.getRouteUnit().getSqlUnit().getSql());
                        //TODO for batch event need split to 2-level records
                        for (int parameterIndex = 0; parameterIndex < event.getParameters().size(); parameterIndex++) {
                            preparedStatement.setObject(parameterIndex + 1, event.getParameters().get(parameterIndex));
                        }
                        preparedStatement.executeUpdate();
                        deliverySuccess = true;
                        transactionLogStorage.remove(event.getId());
                    } catch (final SQLException ex) {
                        log.error(String.format("Delivery times %s error, max try times is %s", i + 1, transactionConfig.getSyncMaxDeliveryTryTimes()), ex);
                    } finally {
                        close(isNewConnection, conn, preparedStatement);
                    }
                }
                return;
            default: 
                throw new UnsupportedOperationException(event.getEventType().toString());
        }
    }
    
    private boolean isProcessContinuously() {
        return SoftTransactionManager.getCurrentTransaction().isPresent()
                && SoftTransactionType.BestEffortsDelivery == SoftTransactionManager.getCurrentTransaction().get().getTransactionType();
    }
    
    private boolean isValidConnection(final Connection conn) {
        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT 1")) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next() && 1 == rs.getInt("1");
            }
        } catch (final SQLException ex) {
            return false;
        }
    }
    
    private void close(final boolean isNewConnection, final Connection conn, final PreparedStatement preparedStatement) {
        if (null != preparedStatement) {
            try {
                preparedStatement.close();
            } catch (final SQLException ex) {
                log.error("PreparedStatement closed error:", ex);
            }
        }
        if (isNewConnection && null != conn) {
            try {
                conn.close();
            } catch (final SQLException ex) {
                log.error("Connection closed error:", ex);
            }
        }
    }
}
