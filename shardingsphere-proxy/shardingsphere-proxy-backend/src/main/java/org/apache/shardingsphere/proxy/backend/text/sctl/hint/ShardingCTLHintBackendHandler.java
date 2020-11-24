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

package org.apache.shardingsphere.proxy.backend.text.sctl.hint;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.HintCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.HintCommandExecutor;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.HintCommandExecutorFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Sharding CTL hint backend handler.
 */
@RequiredArgsConstructor
public final class ShardingCTLHintBackendHandler implements TextProtocolBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private HintCommandExecutor hintCommandExecutor;
    
    @SuppressWarnings("unchecked")
    @Override
    public BackendResponse execute() {
        if (!ProxyContext.getInstance().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)) {
            throw new UnsupportedOperationException(String.format("%s should be true, please check your config", ConfigurationPropertyKey.PROXY_HINT_ENABLED.getKey()));
        }
        Optional<ShardingCTLHintStatement> shardingTCLStatement = new ShardingCTLHintParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            throw new InvalidShardingCTLFormatException(sql);
        }
        HintCommand hintCommand = shardingTCLStatement.get().getHintCommand();
        hintCommandExecutor = HintCommandExecutorFactory.newInstance(hintCommand, backendConnection, sql);
        return hintCommandExecutor.execute(hintCommand);
    }
    
    @Override
    public boolean next() throws SQLException {
        return hintCommandExecutor.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return hintCommandExecutor.getQueryData().getData();
    }
}
