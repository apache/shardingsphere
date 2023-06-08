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

package org.apache.shardingsphere.authority.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authority node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorityNodeConverter {
    
    private static final String ROOT_NODE = "authority/";
    
    private static final String VERSION_NODE_PREFIX = "/rules/" + ROOT_NODE;
    
    /**
     * Get group name path.
     *
     * @return group name path
     */
    public static String getRootNode() {
        return ROOT_NODE;
    }
    
    /**
     * Get version.
     *
     * @param rulePath rule path
     * @return version
     */
    public static Optional<String> getVersion(final String rulePath) {
        Pattern pattern = Pattern.compile(VERSION_NODE_PREFIX + "versions" + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
