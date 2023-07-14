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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.dialect.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.MySQLSessionVariableHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Set charset executor of MySQL.
 */
public final class MySQLSetCharsetExecutor implements MySQLSessionVariableHandler {
    
    @Override
    public void handle(final ConnectionSession connectionSession, final String variableName, final String assignValue) {
        String value = formatValue(assignValue);
        connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(parseCharset(value));
    }
    
    private String formatValue(final String value) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(value) || QuoteCharacter.QUOTE.isWrapped(value) ? value.substring(1, value.length() - 1) : value.trim();
    }
    
    private Charset parseCharset(final String value) {
        switch (value.toLowerCase(Locale.ROOT)) {
            case "default":
                return MySQLConstants.DEFAULT_CHARSET.getCharset();
            case "utf8mb4":
                return StandardCharsets.UTF_8;
            default:
                try {
                    return Charset.forName(value);
                } catch (final IllegalArgumentException ex) {
                    throw new UnknownCharsetException(value.toLowerCase());
                }
        }
    }
    
    @Override
    public String getType() {
        return "charset";
    }
    
    @Override
    public Collection<Object> getTypeAliases() {
        return Collections.singleton("character_set_client");
    }
}
