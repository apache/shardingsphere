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
    
    private final String lineMultiple1 = "key1=$${value1::default_value1}:key2=$${value2::default_value2}:tail";
    
    private final String lineMultiple2 = "key1=$${value1::}:key2=$${value2::}:tail ";
    
    @Test
    void assertParseWithInvalidPattern() {
        assertFalse(URLArgumentLine.parse("invalid").isPresent());
    }
    
    @Test
    void assertReplaceArgumentWithNone() {
        Optional<URLArgumentLine> argLine = URLArgumentLine.parse(line);
        Optional<URLArgumentLine> argLineMultiple1 = URLArgumentLine.parse(lineMultiple1);
        Optional<URLArgumentLine> argLineMultiple2 = URLArgumentLine.parse(lineMultiple2);
        assertTrue(argLine.isPresent());
        assertTrue(argLineMultiple1.isPresent());
        assertTrue(argLineMultiple2.isPresent());
        assertThat(argLine.get().replaceArgument(URLArgumentPlaceholderType.NONE), is("key=default_value"));
        assertThat(argLineMultiple1.get().replaceArgument(URLArgumentPlaceholderType.NONE), is("key1=default_value1:key2=default_value2:tail"));
        assertThat(argLineMultiple2.get().replaceArgument(URLArgumentPlaceholderType.NONE), is("key1=:key2=:tail"));
    }
    
    @Test
    void assertReplaceArgumentWithEnvironment() {
        Optional<URLArgumentLine> argLine = URLArgumentLine.parse(line);
        Optional<URLArgumentLine> argLineMultiple1 = URLArgumentLine.parse(lineMultiple1);
        Optional<URLArgumentLine> argLineMultiple2 = URLArgumentLine.parse(lineMultiple2);
        assertTrue(argLine.isPresent());
        assertTrue(argLineMultiple1.isPresent());
        assertTrue(argLineMultiple2.isPresent());
        assertThat(argLine.get().replaceArgument(URLArgumentPlaceholderType.ENVIRONMENT), is("key=default_value"));
        assertThat(argLineMultiple1.get().replaceArgument(URLArgumentPlaceholderType.ENVIRONMENT), is("key1=default_value1:key2=default_value2:tail"));
        assertThat(argLineMultiple2.get().replaceArgument(URLArgumentPlaceholderType.ENVIRONMENT), is("key1=:key2=:tail"));
    }
    
    @Test
    void assertReplaceArgumentWithSystemProperty() {
        System.setProperty("value", "props_value");
        System.setProperty("value1", "props_value1");
        System.setProperty("value2", "props_value2");
        Optional<URLArgumentLine> argLine = URLArgumentLine.parse(line);
        Optional<URLArgumentLine> argLineMultiple1 = URLArgumentLine.parse(lineMultiple1);
        Optional<URLArgumentLine> argLineMultiple2 = URLArgumentLine.parse(lineMultiple2);
        assertTrue(argLine.isPresent());
        assertTrue(argLineMultiple1.isPresent());
        assertTrue(argLineMultiple2.isPresent());
        assertThat(argLine.get().replaceArgument(URLArgumentPlaceholderType.SYSTEM_PROPS), is("key=props_value"));
        assertThat(argLineMultiple1.get().replaceArgument(URLArgumentPlaceholderType.SYSTEM_PROPS), is("key1=props_value1:key2=props_value2:tail"));
        assertThat(argLineMultiple2.get().replaceArgument(URLArgumentPlaceholderType.SYSTEM_PROPS), is("key1=props_value1:key2=props_value2:tail"));
    }
}
