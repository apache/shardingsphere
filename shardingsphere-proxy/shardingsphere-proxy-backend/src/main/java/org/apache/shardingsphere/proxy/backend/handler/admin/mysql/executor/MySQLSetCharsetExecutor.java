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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor;

import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.admin.mysql.MySQLSessionVariableHandler;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Set charset executor of MySQL.
 */
public final class MySQLSetCharsetExecutor implements MySQLSessionVariableHandler {
    
    private static final Set<String> TYPE_ALIASES = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    private static final Set<String> CHARSET_VARIABLE_NAMES = new HashSet<>(Arrays.asList("charset", "character_set_client"));
    
    static {
        TYPE_ALIASES.add("character_set_client");
    }
    
    @Override
    public void handle(final ConnectionSession connectionSession, final String variableName, final String assignValue) {
        String value = formatValue(assignValue);
        connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(parseCharset(value));
    }
    
    private String getCharacterSetValue(final SetStatement setStatement) {
        return setStatement.getVariableAssigns().stream().filter(each -> CHARSET_VARIABLE_NAMES.contains(each.getVariable().getVariable().toLowerCase(Locale.ROOT)))
                .map(VariableAssignSegment::getAssignValue).findFirst().orElse("");
    }
    
    private String formatValue(final String value) {
        return value.startsWith("'") && value.endsWith("'") || value.startsWith("\"") && value.endsWith("\"") ? value.substring(1, value.length() - 1) : value.trim();
    }
    
    private Charset parseCharset(final String value) {
        switch (value.toLowerCase(Locale.ROOT)) {
            case "default":
                return MySQLServerInfo.DEFAULT_CHARSET.getCharset();
            case "utf8mb4":
                return StandardCharsets.UTF_8;
            default:
                try {
                    return Charset.forName(value);
                    // CHECKSTYLE:OFF
                } catch (Exception ex) {
                    // CHECKSTYLE:ON
                    throw new UnsupportedCharsetException(value.toLowerCase());
                }
        }
    }
    
    @Override
    public String getType() {
        return "charset";
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return TYPE_ALIASES;
    }
}
