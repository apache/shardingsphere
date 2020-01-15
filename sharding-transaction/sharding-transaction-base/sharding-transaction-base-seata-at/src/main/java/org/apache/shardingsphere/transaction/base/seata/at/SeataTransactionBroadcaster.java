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
import org.apache.shardingsphere.underlying.executor.engine.ExecutorDataMap;

import java.util.Map;

/**
 * Seata transaction broadcaster.
 *
 * @author zhaojun
 */
class SeataTransactionBroadcaster {
    
    private static final String SEATA_TX_XID = "SEATA_TX_XID";
    
    static void collectGlobalTxId() {
        if (RootContext.inGlobalTransaction()) {
            ExecutorDataMap.getValue().put(SEATA_TX_XID, RootContext.getXID());
        }
    }
    
    static void broadcastIfNecessary(final Map<String, Object> shardingExecuteDataMap) {
        if (shardingExecuteDataMap.containsKey(SEATA_TX_XID) && !RootContext.inGlobalTransaction()) {
            RootContext.bind((String) shardingExecuteDataMap.get(SEATA_TX_XID));
        }
    }

    static void clear() {
        ExecutorDataMap.getValue().remove(SEATA_TX_XID);
    }
}
