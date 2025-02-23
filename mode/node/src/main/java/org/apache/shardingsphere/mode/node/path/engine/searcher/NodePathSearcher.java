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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Node path searcher.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NodePathSearcher {
    
    /**
     * Find node segment.
     *
     * @param path to be searched path
     * @param criteria node path search criteria
     * @return found node segment
     */
    public static Optional<String> find(final String path, final NodePathSearchCriteria criteria) {
        Matcher matcher = createPattern(criteria.getSearchExample(), criteria.isTrimEmptyNode(), criteria.isContainsChildPath()).matcher(path);
        return matcher.find() ? Optional.of(matcher.group(criteria.getSearchSegmentIndex())) : Optional.empty();
    }
    
    /**
     * Whether to matched path.
     *
     * @param path to be searched path
     * @param criteria node path search criteria
     * @return is matched path or not
     */
    public static boolean isMatchedPath(final String path, final NodePathSearchCriteria criteria) {
        return createPattern(criteria.getSearchExample(), criteria.isTrimEmptyNode(), criteria.isContainsChildPath()).matcher(path).find();
    }
    
    private static Pattern createPattern(final NodePath nodePathCriteria, final boolean trimEmptyNode, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        return Pattern.compile(NodePathGenerator.toPath(nodePathCriteria, trimEmptyNode) + endPattern, Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Get version node path parser.
     *
     * @param searchExample search example
     * @return version node path parser
     */
    public static VersionNodePathParser getVersion(final NodePath searchExample) {
        return new VersionNodePathParser(NodePathGenerator.toPath(searchExample, false));
    }
}
