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

package org.apache.shardingsphere.proxy.backend.text.encoding;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.ClientEncodingResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

import java.sql.SQLException;

/**
 * Set client encoding backend handler.
 */
@RequiredArgsConstructor
public class SetClientEncodingBackendHandler implements TextProtocolBackendHandler {
    
    private static final String CLIENT_ENCODING = "client_encoding";
    
    private final SetStatement setStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        String clientEncoding = setStatement.getVariableAssigns().iterator().next().getAssignValue();
        return new ClientEncodingResponseHeader(clientEncoding.trim().toUpperCase(), setStatement, connectionSession);
    }
    
    /**
     * Is set client encoding.
     *
     * @param setStatement set statement
     * @return is set client encoding or not
     */
    public static boolean isSetClientEncoding(final SetStatement setStatement) {
        return CLIENT_ENCODING.equalsIgnoreCase(setStatement.getVariableAssigns().iterator().next().getVariable().getVariable());
    }
}
