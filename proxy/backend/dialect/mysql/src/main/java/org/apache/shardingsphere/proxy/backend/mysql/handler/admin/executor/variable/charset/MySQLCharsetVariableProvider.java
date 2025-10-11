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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetVariableProvider;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

/**
 * Charset variable provider of MySQL.
 */
public final class MySQLCharsetVariableProvider implements CharsetVariableProvider {
    
    @Override
    public Collection<String> getCharsetVariables() {
        return Arrays.asList("charset", "character_set_client");
    }
    
    @Override
    public Charset parseCharset(final String variableValue) {
        String formattedValue = formatValue(variableValue);
        switch (formattedValue.toLowerCase(Locale.ROOT)) {
            case "default":
                return MySQLConstants.DEFAULT_CHARSET.getCharset();
            case "utf8mb4":
                return StandardCharsets.UTF_8;
            default:
                try {
                    return Charset.forName(formattedValue);
                } catch (final IllegalArgumentException ex) {
                    throw new UnknownCharsetException(formattedValue.toLowerCase(Locale.ROOT));
                }
        }
    }
    
    private String formatValue(final String variableValue) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(variableValue) || QuoteCharacter.QUOTE.isWrapped(variableValue) ? variableValue.substring(1, variableValue.length() - 1) : variableValue.trim();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
