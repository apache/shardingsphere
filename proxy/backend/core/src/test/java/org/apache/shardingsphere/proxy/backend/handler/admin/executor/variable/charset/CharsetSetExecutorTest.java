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

import io.netty.util.Attribute;
import io.netty.util.AttributeMap;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class CharsetSetExecutorTest {
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertSetWhenProviderPresentAndVariableMatched() {
        CharsetVariableProvider provider = mock(CharsetVariableProvider.class);
        when(provider.getCharsetVariables()).thenReturn(Collections.singletonList("character_set_client"));
        when(provider.parseCharset("utf8")).thenReturn(StandardCharsets.UTF_8);
        when(DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType)).thenReturn(Optional.of(provider));
        AttributeMap attributeMap = mock(AttributeMap.class);
        Attribute attribute = mock(Attribute.class);
        when(attributeMap.attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).thenReturn(attribute);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        new CharsetSetExecutor(databaseType, connectionSession).set("CHARACTER_SET_CLIENT", "utf8");
        verify(attribute).set(StandardCharsets.UTF_8);
    }
    
    @Test
    void assertSetWhenProviderAbsentOrVariableMismatch() {
        when(DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> new CharsetSetExecutor(databaseType, mock()).set("other_variable", "utf8"));
    }
    
    @Test
    void assertSetWhenVariableNotMatched() {
        CharsetVariableProvider provider = mock(CharsetVariableProvider.class);
        when(provider.getCharsetVariables()).thenReturn(Collections.singletonList("character_set_server"));
        when(DatabaseTypedSPILoader.findService(CharsetVariableProvider.class, databaseType)).thenReturn(Optional.of(provider));
        assertDoesNotThrow(() -> new CharsetSetExecutor(databaseType, mock()).set("character_set_client", "utf8"));
    }
}
