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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.variable.charset;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetVariableProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdCharsetVariableProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final CharsetVariableProvider provider = DatabaseTypedSPILoader.getService(CharsetVariableProvider.class, databaseType);
    
    @Test
    void assertGetCharsetVariables() {
        assertThat(provider.getCharsetVariables(), is(Collections.singleton("names")));
    }
    
    @Test
    void assertParseDefaultCharset() {
        assertThat(provider.parseCharset(" default "), is(Charset.defaultCharset()));
    }
    
    @Test
    void assertParseKnownCharset() {
        assertThat(provider.parseCharset("utf8"), is(StandardCharsets.UTF_8));
    }
    
    @Test
    void assertParseInvalidCharset() {
        InvalidParameterValueException ex = assertThrows(InvalidParameterValueException.class, () -> provider.parseCharset("unknown_charset"));
        assertThat(ex.getParameterName(), is("names"));
        assertThat(ex.getParameterValue(), is("unknown_charset"));
    }
}
