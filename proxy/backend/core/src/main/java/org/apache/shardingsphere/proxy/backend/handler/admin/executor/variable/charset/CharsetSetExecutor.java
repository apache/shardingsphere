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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Set charset executor.
 */
@RequiredArgsConstructor
public final class CharsetSetExecutor {
    
    private final DatabaseType databaseType;
    
    private final ConnectionSession connectionSession;
    
    /**
     * Set charset.
     *
     * @param variableName variable name
     * @param assignValue assign value
     */
    public void set(final String variableName, final String assignValue) {
        Optional<CharsetVariableProvider> charsetVariableProvider = DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType);
        if (charsetVariableProvider.isPresent() && charsetVariableProvider.get().isCharsetVariable(variableName)) {
            connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(charsetVariableProvider.get().parseCharset(assignValue));
        }
    }
    
    /**
     * Set charset.
     *
     * @param variables variables
     */
    public void set(final Map<String, String> variables) {
        Optional<CharsetVariableProvider> charsetVariableProvider = DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType);
        if (!charsetVariableProvider.isPresent()) {
            return;
        }
        for (Entry<String, String> entry : variables.entrySet()) {
            if (charsetVariableProvider.get().isCharsetVariable(entry.getKey())) {
                connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(charsetVariableProvider.get().parseCharset(entry.getValue()));
            }
        }
    }
}
