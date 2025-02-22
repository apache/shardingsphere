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

package org.apache.shardingsphere.mode.node.path;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NodePathGenerator {
    
    private static final String PATH_DELIMITER = "/";
    
    private static final String VARIABLE_PREFIX = "${";
    
    private static final String VARIABLE_SUFFIX = "}";
    
    private static final String COLON_DELIMITER = ":";
    
    private static final Pattern PATH_SPLITTER = Pattern.compile(PATH_DELIMITER);
    
    private static final Pattern COLON_SPLITTER = Pattern.compile(COLON_DELIMITER);
    
    /**
     * Generate to path.
     *
     * @param nodePath node path
     * @param trimEmptyNode null variable should trim parent node if true
     * @return path
     */
    public static String toPath(final NodePath nodePath, final boolean trimEmptyNode) {
        String templatePath = Objects.requireNonNull(nodePath.getClass().getAnnotation(NodePathEntity.class), "NodePathEntity annotation is missing").value();
        LinkedList<String> resolvedSegments = new LinkedList<>();
        for (String each : PATH_SPLITTER.split(templatePath)) {
            if (each.contains(VARIABLE_PREFIX) || each.contains(COLON_DELIMITER)) {
                Collection<String> nodeSegments = new LinkedList<>();
                for (String eachSegment : COLON_SPLITTER.split(each)) {
                    if (eachSegment.startsWith(VARIABLE_PREFIX) && eachSegment.endsWith(VARIABLE_SUFFIX)) {
                        Object fieldValue = ReflectionUtils.getFieldValue(nodePath, eachSegment.substring(2, eachSegment.length() - 1)).orElse(null);
                        // CHECKSTYLE:OFF
                        if (null == fieldValue) {
                            if (trimEmptyNode) {
                                resolvedSegments.removeLast();
                            }
                            return String.join(PATH_DELIMITER, resolvedSegments);
                        }
                        // CHECKSTYLE:ON
                        nodeSegments.add(fieldValue.toString());
                    } else {
                        nodeSegments.add(each);
                    }
                }
                resolvedSegments.add(String.join(COLON_DELIMITER, nodeSegments));
            } else {
                resolvedSegments.add(each);
            }
        }
        return String.join(PATH_DELIMITER, resolvedSegments);
    }
    
    /**
     * Generate to version node path.
     *
     * @param nodePath node path
     * @return version node path
     */
    public static VersionNodePath toVersionPath(final NodePath nodePath) {
        return new VersionNodePath(toPath(nodePath, false));
    }
}
