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

package org.apache.shardingsphere.mode.node.path.engine.searcher;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodePathPatternTest {
    
    @Test
    void assertIdentifier() {
        Pattern pattern = Pattern.compile(NodePathPattern.IDENTIFIER);
        assertTrue(pattern.matcher("foo").matches());
        assertTrue(pattern.matcher("foo1").matches());
        assertTrue(pattern.matcher("foo_bar").matches());
        assertTrue(pattern.matcher("foo-bar").matches());
        assertFalse(pattern.matcher("foo.bar").matches());
        assertFalse(pattern.matcher("#foo").matches());
    }
    
    @Test
    void assertQualifiedIdentifier() {
        Pattern pattern = Pattern.compile(NodePathPattern.QUALIFIED_IDENTIFIER);
        assertTrue(pattern.matcher("foo").matches());
        assertTrue(pattern.matcher("foo1").matches());
        assertTrue(pattern.matcher("foo_bar").matches());
        assertTrue(pattern.matcher("foo-bar").matches());
        assertTrue(pattern.matcher("foo.bar").matches());
        assertFalse(pattern.matcher("#foo").matches());
        assertTrue(pattern.matcher("foo:foo_bar").matches());
    }
}
