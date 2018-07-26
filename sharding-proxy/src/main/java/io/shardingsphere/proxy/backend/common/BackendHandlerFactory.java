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

package io.shardingsphere.proxy.backend.common;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.proxy.backend.common.jdbc.BackendConnection;
import io.shardingsphere.proxy.backend.common.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.common.jdbc.execute.JDBCExecuteEngineFactory;
import io.shardingsphere.proxy.backend.common.netty.SQLPacketsBackendHandler;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.CommandPacketRebuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Backend handler factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendHandlerFactory {
    
    /**
     * Create new instance of text protocol backend handler.
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @param rebuilder rebuilder
     * @return instance of text protocol backend handler
     */
    public static BackendHandler newTextProtocolInstance(final String sql, final BackendConnection backendConnection, final DatabaseType databaseType, final CommandPacketRebuilder rebuilder) {
        return RuleRegistry.getInstance().isProxyBackendUseNio()
                ? new SQLPacketsBackendHandler(rebuilder, databaseType) : new JDBCBackendHandler(sql, JDBCExecuteEngineFactory.createTextProtocolInstance(backendConnection));
    }
}
