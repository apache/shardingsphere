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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.variable.charset;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetVariableProvider;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Charset variable provider of PostgreSQL.
 */
public final class PostgreSQLCharsetVariableProvider implements CharsetVariableProvider {
    
    @Override
    public Collection<String> getCharsetVariables() {
        return Collections.singleton("client_encoding");
    }
    
    @Override
    public Charset parseCharset(final String variableValue) {
        String formattedValue = formatValue(variableValue).toLowerCase(Locale.ROOT);
        boolean isDefault = "default".equals(formattedValue);
        boolean isUtf8 = "utf8".equals(formattedValue) || "utf-8".equals(formattedValue) || "utf_8".equals(formattedValue) || "unicode".equals(formattedValue);
        if (!isDefault && !isUtf8) {
            throw new InvalidParameterValueException("client_encoding", formattedValue);
        }
        return StandardCharsets.UTF_8;
    }
    
    private String formatValue(final String value) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(value) || QuoteCharacter.QUOTE.isWrapped(value) ? value.substring(1, value.length() - 1).trim() : value.trim();
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
