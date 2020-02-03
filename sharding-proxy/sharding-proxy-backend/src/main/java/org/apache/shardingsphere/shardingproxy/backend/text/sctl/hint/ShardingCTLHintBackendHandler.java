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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint;

import com.google.common.base.Optional;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommandExecutor;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommandExecutorFactory;

import java.sql.SQLException;

/**
 * Sharding CTL hint backend handler.
 *
 * @author liya
 */
public final class ShardingCTLHintBackendHandler implements TextProtocolBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private HintCommandExecutor hintCommandExecutor;
    
    public ShardingCTLHintBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql;
        this.backendConnection = backendConnection;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BackendResponse execute() {
        if (!backendConnection.isSupportHint()) {
            throw new UnsupportedOperationException(String.format("%s should be true, please check your config", PropertiesConstant.PROXY_HINT_ENABLED.getKey()));
        }
        Optional<ShardingCTLHintStatement> shardingTCLStatement = new ShardingCTLHintParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            return new ErrorResponse(new InvalidShardingCTLFormatException(sql));
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
    public QueryData getQueryData() throws SQLException {
        return hintCommandExecutor.getQueryData();
    }
}
