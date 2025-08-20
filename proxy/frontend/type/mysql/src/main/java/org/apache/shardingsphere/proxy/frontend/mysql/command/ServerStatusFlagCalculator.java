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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * MySQL server status flag calculator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerStatusFlagCalculator {
    
    /**
     * Calculate server status flag for specified connection.
     *
     * @param connectionSession connection session
     * @param lastPacket last packet
     * @return server status flag
     */
    public static int calculateFor(final ConnectionSession connectionSession, final boolean lastPacket) {
        int result = 0;
        result |= connectionSession.isAutoCommit() ? MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue() : 0;
        result |= connectionSession.getTransactionStatus().isInTransaction() ? MySQLStatusFlag.SERVER_STATUS_IN_TRANS.getValue() : 0;
        result |= lastPacket ? 0 : MySQLStatusFlag.SERVER_MORE_RESULTS_EXISTS.getValue();
        return result;
    }
}
