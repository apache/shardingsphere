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

package org.apache.shardingsphere.mode.node.path.engine.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NodePathSegmentTest {
    
    @Test
    void assertGetLiteralWithoutVariable() {
        assertThat(new NodePathSegment("foo").getLiteral(new NodePathFixture("foo")), is(Optional.of("foo")));
    }
    
    @Test
    void assertGetLiteralWithVariable() {
        assertThat(new NodePathSegment("${fooVariable}").getLiteral(new NodePathFixture("foo_value")), is(Optional.of("foo_value")));
    }
    
    @Test
    void assertGetLiteralWithNullVariable() {
        assertFalse(new NodePathSegment("${fooVariable}").getLiteral(new NodePathFixture(null)).isPresent());
    }
    
    @NodePathEntity("/foo/${fooVariable}")
    @RequiredArgsConstructor
    @Getter
    private static final class NodePathFixture implements NodePath {
        
        private final String fooVariable;
    }
}
