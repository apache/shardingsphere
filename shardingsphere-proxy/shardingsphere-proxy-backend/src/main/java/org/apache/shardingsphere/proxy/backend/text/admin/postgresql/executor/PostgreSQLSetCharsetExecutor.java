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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor;

import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.proxy.backend.exception.InvalidParameterValueException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.PostgreSQLCharacterSets;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.PostgreSQLSessionVariableHandler;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Set charset executor of PostgreSQL.
 */
public final class PostgreSQLSetCharsetExecutor implements PostgreSQLSessionVariableHandler {
    
    @Override
    public void handle(final ConnectionSession connectionSession, final SetStatement setStatement) {
        VariableAssignSegment segment = setStatement.getVariableAssigns().iterator().next();
        String value = formatValue(segment.getAssignValue().trim());
        connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(parseCharset(value));
    }
    
    private String formatValue(final String value) {
        return value.startsWith("'") && value.endsWith("'") ? value.substring(1, value.length() - 1).trim() : value;
    }
    
    private Charset parseCharset(final String value) {
        try {
            String result = value.toLowerCase(Locale.ROOT);
            return "default".equals(result) ? StandardCharsets.UTF_8 : PostgreSQLCharacterSets.findCharacterSet(result);
        } catch (final IllegalArgumentException ignored) {
            throw new InvalidParameterValueException("client_encoding", value.toLowerCase(Locale.ROOT));
        }
    }
    
    @Override
    public String getType() {
        return "client_encoding";
    }
}
