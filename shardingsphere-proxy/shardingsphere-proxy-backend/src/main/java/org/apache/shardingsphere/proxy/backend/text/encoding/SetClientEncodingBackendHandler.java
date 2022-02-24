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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Set client encoding backend handler.
 */
@RequiredArgsConstructor
public class SetClientEncodingBackendHandler implements TextProtocolBackendHandler {
    
    private static final String MYSQL_KEY = "charset";
    
    private static final String POSTGRESQL_KEY = "client_encoding";
    
    private final SetStatement setStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        VariableAssignSegment segment = setStatement.getVariableAssigns().iterator().next();
        return new ClientEncodingResponseHeader(segment.getVariable().getVariable().trim(), formatValue(segment.getAssignValue().trim()), setStatement, connectionSession);
    }
    
    private String formatValue(final String value) {
        return value.startsWith("'") && value.endsWith("'") ? value.substring(1, value.length() - 1) : value.trim();
    }
    
    /**
     * Judgment set statement Is set client encoding set statement.
     *
     * @param setStatement set statement
     * @return is set client encoding or not
     */
    public static boolean isSetClientEncoding(final SetStatement setStatement) {
        Iterator<VariableAssignSegment> iterator = setStatement.getVariableAssigns().iterator();
        return iterator.hasNext() && containsKey(iterator.next().getVariable().getVariable());
    }
    
    private static boolean containsKey(final String key) {
        return MYSQL_KEY.equalsIgnoreCase(key) || POSTGRESQL_KEY.equalsIgnoreCase(key);
    }
}
