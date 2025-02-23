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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.NodePathEntity;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

/**
 * Node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NodePathGenerator {
    
    private static final String PATH_DELIMITER = "/";
    
    /**
     * Generate to path.
     *
     * @param nodePath node path
     * @param trimEmptyNode null variable should trim parent node if true
     * @return path
     */
    public static String toPath(final NodePath nodePath, final boolean trimEmptyNode) {
        String templatePath = Objects.requireNonNull(nodePath.getClass().getAnnotation(NodePathEntity.class), "NodePathEntity annotation is missing").value();
        LinkedList<String> nodeSegments = new LinkedList<>();
        for (String each : templatePath.split(PATH_DELIMITER)) {
            Optional<String> segmentLiteral = new NodePathSegment(each).getLiteral(nodePath);
            if (segmentLiteral.isPresent()) {
                nodeSegments.add(segmentLiteral.get());
                continue;
            }
            if (trimEmptyNode) {
                trimLastParentNode(nodeSegments);
            }
            break;
        }
        return String.join(PATH_DELIMITER, nodeSegments);
    }
    
    private static void trimLastParentNode(final LinkedList<String> nodeSegments) {
        if (!nodeSegments.isEmpty()) {
            nodeSegments.removeLast();
        }
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
