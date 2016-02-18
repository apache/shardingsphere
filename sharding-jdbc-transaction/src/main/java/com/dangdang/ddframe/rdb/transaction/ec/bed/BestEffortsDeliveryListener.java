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

package com.dangdang.ddframe.rdb.transaction.ec.bed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.transaction.ec.api.EventualConsistencyTransactionManager;
import com.dangdang.ddframe.rdb.transaction.ec.api.EventualConsistencyTransactionType;
import com.dangdang.ddframe.rdb.transaction.ec.storage.MemoryTransacationLogStorage;
import com.dangdang.ddframe.rdb.transaction.ec.storage.TransacationLogStorage;
import com.dangdang.ddframe.rdb.transaction.ec.storage.TransactionLog;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

/**
 * 最大努力送达型事务监听器.
 * 
 * @author zhangliang
 */
@Slf4j
public final class BestEffortsDeliveryListener {
    
    private final TransacationLogStorage transacationLogStorage = new MemoryTransacationLogStorage();
    
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final DMLExecutionEvent event) {
        if (EventualConsistencyTransactionType.BestEffortsDelivery != EventualConsistencyTransactionManager.getTransactionType()) {
            return;
        }
        switch (event.getEventExecutionType()) {
            case BEFORE_EXECUTE: 
                transacationLogStorage.add(new TransactionLog(event.getId(), EventualConsistencyTransactionManager.getTransactionId(), 
                        EventualConsistencyTransactionManager.getTransactionType(), event.getDataSource(), event.getSql(), event.getParameters()));
                break;
            case EXECUTE_SUCCESS: 
                transacationLogStorage.remove(event.getId());
                break;
            case EXECUTE_FAILURE: 
                boolean deliverySuccess = false;
                int syncMaxDeliveryTryTimes = EventualConsistencyTransactionManager.getTransactionConfiguration().getSyncMaxDeliveryTryTimes();
                for (int i = 0; i < syncMaxDeliveryTryTimes; i++) {
                    if (deliverySuccess) {
                        return;
                    }
                    boolean isNewConnection = false;
                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    try {
                        conn = EventualConsistencyTransactionManager.getConnection().getConnection(event.getDataSource());
                        if (!isValidConnection(conn)) {
                            conn = EventualConsistencyTransactionManager.getConnection().getNewConnection(event.getDataSource());
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
                        log.error(String.format("delivery times %s error, max try times is %s", i + 1, syncMaxDeliveryTryTimes), ex);
                    } finally {
                        close(isNewConnection, conn, pstmt);
                    }
                }
                break;
            default: 
                throw new UnsupportedOperationException(event.getEventExecutionType().toString());
        }
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
}
