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

package org.apache.shardingsphere.infra.url.core.arg;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URLArgumentLineTest {
    
    private final String line = "key=$${value::default_value}";
    
    @Test
    void assertParseWithInvalidPattern() {
        assertFalse(URLArgumentLine.parse("invalid").isPresent());
    }
    
    @Test
    void assertReplaceArgumentWithNone() {
        Optional<URLArgumentLine> argLine = URLArgumentLine.parse(line);
        assertTrue(argLine.isPresent());
        assertThat(argLine.get().replaceArgument(URLArgumentPlaceholderType.NONE), is("key=default_value"));
    }
    
    @Test
    void assertReplaceArgumentWithEnvironment() {
        Optional<URLArgumentLine> argLine = URLArgumentLine.parse(line);
        assertTrue(argLine.isPresent());
        assertThat(argLine.get().replaceArgument(URLArgumentPlaceholderType.ENVIRONMENT), is("key=default_value"));
    }
    
    @Test
    void assertReplaceArgumentWithSystemProperty() {
        System.setProperty("value", "props_value");
        Optional<URLArgumentLine> argLine = URLArgumentLine.parse(line);
        assertTrue(argLine.isPresent());
        assertThat(argLine.get().replaceArgument(URLArgumentPlaceholderType.SYSTEM_PROPS), is("key=props_value"));
    }
}
