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

import io.seata.core.context.RootContext;
import org.apache.shardingsphere.infra.executor.sql.hook.SQLExecutionHook;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorDataMap;

import java.util.List;
import java.util.Map;

/**
 * Seata transactional SQL execution hook.
 */
public final class TransactionalSQLExecutionHook implements SQLExecutionHook {
    
    private static final String SEATA_TX_XID = "SEATA_TX_XID";
    
    private boolean seataBranch;
    
    @Override
    public void start(final String dataSourceName, final String sql, final List<Object> parameters,
                      final DataSourceMetaData dataSourceMetaData, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
        if (isTrunkThread) {
            if (RootContext.inGlobalTransaction()) {
                ExecutorDataMap.getValue().put(SEATA_TX_XID, RootContext.getXID());
            }
        } else if (!RootContext.inGlobalTransaction() && shardingExecuteDataMap.containsKey(SEATA_TX_XID)) {
            RootContext.bind((String) shardingExecuteDataMap.get(SEATA_TX_XID));
            seataBranch = true;
        }
    }
    
    @Override
    public void finishSuccess() {
        if (seataBranch) {
            RootContext.unbind();
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        if (seataBranch) {
            RootContext.unbind();
        }
    }
}
