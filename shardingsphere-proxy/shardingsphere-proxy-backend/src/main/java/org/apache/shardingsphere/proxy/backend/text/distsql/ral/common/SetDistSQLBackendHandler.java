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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.SetDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.variable.SetVariableStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.SQLException;

/**
 * Set dist sql backend handler.
 */
@RequiredArgsConstructor
@Getter
public final class SetDistSQLBackendHandler implements TextProtocolBackendHandler {
    
    private final SetDistSQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (!(sqlStatement instanceof SetVariableStatement)) {
            throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
        }
        SetVariableStatement setVariableStatement = (SetVariableStatement) sqlStatement;
        if (VariableEnum.TRANSACTION_TYPE == VariableEnum.getValueOf(setVariableStatement.getName())) {
            backendConnection.getTransactionStatus().setTransactionType(getTransactionType(setVariableStatement.getValue()));
            return new UpdateResponseHeader(sqlStatement);
        }
        throw new UnsupportedVariableException(setVariableStatement.getName());
    }
    
    private TransactionType getTransactionType(final String transactionTypeName) throws UnsupportedVariableException {
        try {
            return TransactionType.valueOf(transactionTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedVariableException(transactionTypeName);
        }
    }
}
