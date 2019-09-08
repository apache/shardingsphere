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
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintType;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddDatabaseShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddTableShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintClearCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetDatabaseShardingValueCommand;

/**
 * Sharding CTL hint backend handler.
 *
 * @author liya
 */
public final class ShardingCTLHintBackendHandler implements TextProtocolBackendHandler {

    private static final ThreadLocal<HintManager> HINT_MANAGER_HOLDER = new ThreadLocal<>();

    private final String sql;

    private final boolean supportHint;

    private HintManager hintManager;

    public ShardingCTLHintBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql;
        this.supportHint = backendConnection.isSupportHint();
        initHintManagerr(supportHint);
    }

    private void initHintManagerr(final boolean supportHint) {
        if (!supportHint) {
            return;
        }
        if (HINT_MANAGER_HOLDER.get() == null) {
            HINT_MANAGER_HOLDER.set(HintManager.getInstance());
        }
        hintManager = HINT_MANAGER_HOLDER.get();
    }

    @Override
    public BackendResponse execute() {
        if (!supportHint) {
            throw new UnsupportedOperationException(String.format("%s should be true, please check your config", ShardingPropertiesConstant.PROXY_HINT_ENABLED.getKey()));
        }
        Optional<ShardingCTLHintStatement> shardingTCLStatement = new ShardingCTLHintParser(sql).doParse();
        if (!shardingTCLStatement.isPresent()) {
            return new ErrorResponse(new InvalidShardingCTLFormatException(sql));
        }
        HintCommand hintCommand = shardingTCLStatement.get().getHintCommand();
        if (hintCommand instanceof HintSetCommand) {
            handlerSetCommand((HintSetCommand) hintCommand);
        } else if (hintCommand instanceof HintAddDatabaseShardingValueCommand) {
            HintAddDatabaseShardingValueCommand shardingValueCommand = (HintAddDatabaseShardingValueCommand) hintCommand;
            hintManager.addDatabaseShardingValue(shardingValueCommand.getLogicTable(), shardingValueCommand.getValue());
        } else if (hintCommand instanceof HintAddTableShardingValueCommand) {
            HintAddTableShardingValueCommand shardingValueCommand = (HintAddTableShardingValueCommand) hintCommand;
            hintManager.addTableShardingValue(shardingValueCommand.getLogicTable(), shardingValueCommand.getValue());
        } else if (hintCommand instanceof HintSetDatabaseShardingValueCommand) {
            hintManager.setDatabaseShardingValue(((HintSetDatabaseShardingValueCommand) hintCommand).getValue());
        } else if (hintCommand instanceof HintClearCommand) {
            handlerHintClearCommand();
        } else {
            return new ErrorResponse(new UnsupportedShardingCTLTypeException(sql));
        }
        return new UpdateResponse();
    }

    private void handlerSetCommand(final HintSetCommand hintCommand) {
        HintType hintType = hintCommand.getHintType();
        switch (hintType) {
            case MASTER_ONLY:
                hintManager.setMasterRouteOnly();
                break;
            default:
                break;
        }
    }

    private void handlerHintClearCommand() {
        HINT_MANAGER_HOLDER.remove();
        HintManager.clear();
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public QueryData getQueryData() {
        return null;
    }
}
