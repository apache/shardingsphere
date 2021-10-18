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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;

/**
 * Set variable statement executor.
 */
@AllArgsConstructor
public final class SetVariableExecutor implements SetStatementExecutor {
    
    private final SetVariableStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() {
        SetVariableStatement setVariableStatement = sqlStatement;
        VariableEnum variable = VariableEnum.getValueOf(setVariableStatement.getName());
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                Boolean agentPluginsEnabled = BooleanUtils.toBooleanObject(setVariableStatement.getValue());
                SystemPropertyUtil.setSystemProperty(variable.name(), null == agentPluginsEnabled ? Boolean.FALSE.toString() : agentPluginsEnabled.toString());
                break;
            case TRANSACTION_TYPE:
                backendConnection.getTransactionStatus().setTransactionType(getTransactionType(setVariableStatement.getValue()));
                break;
            default:
                throw new UnsupportedVariableException(setVariableStatement.getName());
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private TransactionType getTransactionType(final String transactionTypeName) throws UnsupportedVariableException {
        try {
            return TransactionType.valueOf(transactionTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedVariableException(transactionTypeName);
        }
    }
}
