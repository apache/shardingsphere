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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodePathSearcherTest {
    
    @Test
    void assertFindReturnsEmptyWhenPathNotMatched() {
        NodePathSearchCriteria criteria = new NodePathSearchCriteria(new SearchExampleNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), false, 1);
        Optional<String> actual = NodePathSearcher.find("/invalid/path", criteria);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetReturnsMatchedSegment() {
        NodePathSearchCriteria criteria = new NodePathSearchCriteria(new SearchExampleNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), false, 2);
        String actual = NodePathSearcher.get("/search/foo/child/bar", criteria);
        assertThat(actual, is("bar"));
    }
    
    @Test
    void assertGetThrowsExceptionWhenNotMatched() {
        NodePathSearchCriteria criteria = new NodePathSearchCriteria(new SearchExampleNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), false, 1);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> NodePathSearcher.get("/search/foo", criteria));
        assertThat(actual.getMessage(), is("Can not find node segment in path: /search/foo"));
    }
    
    @Test
    void assertIsMatchedPathWhenContainsChildPath() {
        NodePathSearchCriteria criteria = new NodePathSearchCriteria(new SearchExampleNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), true, 1);
        boolean actual = NodePathSearcher.isMatchedPath("/search/foo/child/bar/extra", criteria);
        assertTrue(actual);
    }
    
    @Test
    void assertIsMatchedPathReturnsFalseWhenPathMismatch() {
        NodePathSearchCriteria criteria = new NodePathSearchCriteria(new SearchExampleNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), true, 1);
        boolean actual = NodePathSearcher.isMatchedPath("/mismatch/foo", criteria);
        assertFalse(actual);
    }
    
    @NodePathEntity("/search/${first}/child/${second}")
    @RequiredArgsConstructor
    @Getter
    private static final class SearchExampleNodePath implements NodePath {
        
        private final String first;
        
        private final String second;
    }
}
