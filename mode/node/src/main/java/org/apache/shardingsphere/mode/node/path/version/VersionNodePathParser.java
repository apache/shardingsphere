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

package org.apache.shardingsphere.mode.node.path.version;

import org.apache.shardingsphere.mode.node.path.NodePath;

import java.util.regex.Pattern;

/**
 * Version node path parser.
 */
public final class VersionNodePathParser {
    
    private final Pattern activeVersionPattern;
    
    public VersionNodePathParser(final NodePath nodePath) {
        VersionNodePath versionNodePath = new VersionNodePath(nodePath);
        activeVersionPattern = Pattern.compile(versionNodePath.getActiveVersionPath() + "$", Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Judge whether to active version path.
     *
     * @param path to be judged path
     * @return is active version path or not
     */
    public boolean isActiveVersionPath(final String path) {
        return activeVersionPattern.matcher(path).find();
    }
}
