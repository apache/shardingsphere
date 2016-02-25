/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.transaction.soft.bed.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEventListener;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionManager;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionManagerFactory;
import com.dangdang.ddframe.rdb.transaction.soft.api.SoftTransactionType;
import com.dangdang.ddframe.rdb.transaction.soft.api.config.SoftTransactionConfiguration;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransacationLogStorage;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransacationLogStorageFactory;
import com.dangdang.ddframe.rdb.transaction.soft.storage.TransactionLog;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

/**
 * 最大努力送达型事务监听器.
 * 
 * @author zhangliang
 */
@Slf4j
public final class BestEffortsDeliveryListener implements DMLExecutionEventListener {
    
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final DMLExecutionEvent event) {
        if (!isProcessContinuously()) {
            return;
        }
        SoftTransactionConfiguration transactionConfig = SoftTransactionManagerFactory.getCurrentTransactionConfiguration().get();
        TransacationLogStorage transacationLogStorage = TransacationLogStorageFactory.createTransacationLogStorageFactory(transactionConfig);
        SoftTransactionManager transactionManager = SoftTransactionManagerFactory.getCurrentTransactionManager().get();
        switch (event.getEventExecutionType()) {
            case BEFORE_EXECUTE: 
                transacationLogStorage.add(new TransactionLog(
                        event.getId(), transactionManager.getTransactionId(), transactionManager.getTransactionType(), event.getDataSource(), event.getSql(), event.getParameters(), 0));
                return;
            case EXECUTE_SUCCESS: 
                transacationLogStorage.remove(event.getId());
                return;
            case EXECUTE_FAILURE: 
                boolean deliverySuccess = false;
                for (int i = 0; i < transactionConfig.getSyncMaxDeliveryTryTimes(); i++) {
                    if (deliverySuccess) {
                        return;
                    }
                    boolean isNewConnection = false;
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    try {
                        conn = transactionManager.getConnection().getConnection(event.getDataSource());
                        if (!isValidConnection(conn)) {
                            conn = transactionManager.getConnection().getNewConnection(event.getDataSource());
                            isNewConnection = true;
                        }
                        pstmt = conn.prepareStatement(event.getSql());
                        for (int parameterIndex = 0; parameterIndex < event.getParameters().size(); parameterIndex++) {
                            pstmt.setObject(parameterIndex + 1, event.getParameters().get(parameterIndex));
                        }
                        pstmt.executeUpdate();
                        deliverySuccess = true;
                        transacationLogStorage.remove(event.getId());
                    } catch (final SQLException ex) {
                        log.error(String.format("Delivery times %s error, max try times is %s", i + 1, transactionConfig.getSyncMaxDeliveryTryTimes()), ex);
                    } finally {
                        close(isNewConnection, conn, pstmt);
                    }
                }
                return;
            default: 
                throw new UnsupportedOperationException(event.getEventExecutionType().toString());
        }
    }
    
    private boolean isProcessContinuously() {
        return SoftTransactionManagerFactory.getCurrentTransactionManager().isPresent()
                && SoftTransactionType.BestEffortsDelivery == SoftTransactionManagerFactory.getCurrentTransactionManager().get().getTransactionType();
    }
    
    private boolean isValidConnection(final Connection conn) {
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT 1")) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return 1 == rs.getInt("1");
                }
                return false;
            }
        } catch (final SQLException ex) {
            return false;
        }
    }
    
    private void close(final boolean isNewConnection, final Connection conn, final PreparedStatement pstmt) {
        if (null != pstmt) {
            try {
                pstmt.close();
            } catch (final SQLException ex) {
                log.error("PreparedStatement colsed error:", ex);
            }
        }
        if (isNewConnection && null != conn) {
            try {
                conn.close();
            } catch (final SQLException ex) {
                log.error("Connection colsed error:", ex);
            }
        }
    }
    
    @Override
    public String getName() {
        return getClass().getName();
    }
}
