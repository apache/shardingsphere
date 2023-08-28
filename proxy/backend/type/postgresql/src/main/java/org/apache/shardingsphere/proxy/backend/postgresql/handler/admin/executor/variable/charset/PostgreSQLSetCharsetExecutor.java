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

import org.apache.shardingsphere.infra.exception.dialect.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetVariableProvider;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Set charset executor of PostgreSQL.
 */
public final class PostgreSQLSetCharsetExecutor implements CharsetVariableProvider {
    
    @Override
    public boolean isCharsetVariable(final String variableName) {
        return "client_encoding".equalsIgnoreCase(variableName);
    }
    
    @Override
    public Charset parseCharset(final String variableValue) {
        String formattedValue = formatValue(variableValue);
        try {
            String result = formattedValue.toLowerCase(Locale.ROOT);
            return "default".equals(result) ? StandardCharsets.UTF_8 : PostgreSQLCharacterSets.findCharacterSet(result);
        } catch (final IllegalArgumentException ignored) {
            throw new InvalidParameterValueException("client_encoding", formattedValue.toLowerCase(Locale.ROOT));
        }
    }
    
    private String formatValue(final String variableValue) {
        return variableValue.trim();
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
