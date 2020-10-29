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

package org.apache.shardingsphere.proxy.backend.text.sctl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.explain.ShardingCTLExplainBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.ShardingCTLHintBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.set.ShardingCTLSetBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.show.ShardingCTLShowBackendHandler;

/**
 * Sharding CTL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingCTLBackendHandlerFactory {
    
    public static final String SCTL = "SCTL:";
    
    private static final String SCTL_SET = SCTL + "SET";
    
    private static final String SCTL_SHOW = SCTL + "SHOW";

    private static final String SCTL_EXPLAIN = SCTL + "EXPLAIN";

    private static final String SCTL_HINT = SCTL + "HINT";
    
    /**
     * Create new instance of sharding CTL backend handler.
     *
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @return instance of sharding CTL backend handler
     */
    public static TextProtocolBackendHandler newInstance(final String sql, final BackendConnection backendConnection) {
        if (sql.toUpperCase().startsWith(SCTL_SET)) {
            return new ShardingCTLSetBackendHandler(sql, backendConnection);
        }
        if (sql.toUpperCase().startsWith(SCTL_SHOW)) {
            return new ShardingCTLShowBackendHandler(sql, backendConnection);
        }
        if (sql.toUpperCase().startsWith(SCTL_EXPLAIN)) {
            return new ShardingCTLExplainBackendHandler(sql, backendConnection);
        }
        if (sql.toUpperCase().startsWith(SCTL_HINT)) {
            return new ShardingCTLHintBackendHandler(sql, backendConnection);
        }
        throw new IllegalArgumentException(sql);
    }
}
