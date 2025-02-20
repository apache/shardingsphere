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

import java.util.LinkedList;

/**
 * Node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewNodePathGenerator {
    
    /**
     * Generate path.
     * 
     * @param nodePath node path
     * @param trimEmptyNode whether to trim empty node
     * @return path
     */
    public static String generatePath(final NewNodePath nodePath, final boolean trimEmptyNode) {
        LinkedList<String> result = new LinkedList<>();
        String path = nodePath.getClass().getAnnotation(NodePathEntity.class).value();
        for (String each : path.split("/")) {
            if (each.startsWith("${") && each.endsWith("}")) {
                Object fieldValue = ReflectionUtils.getFieldValue(nodePath, each.substring(2, each.length() - 1)).orElse(null);
                if (null == fieldValue) {
                    if (trimEmptyNode) {
                        result.removeLast();
                    }
                    return String.join("/", result);
                }
                result.add(fieldValue.toString());
            } else {
                result.add(each);
            }
        }
        return String.join("/", result);
    }
    
    /**
     * Generate version node path.
     *
     * @param nodePath node path
     * @return version node path
     */
    public static VersionNodePath generateVersionPath(final NewNodePath nodePath) {
        return new VersionNodePath(generatePath(nodePath, false));
    }
}
