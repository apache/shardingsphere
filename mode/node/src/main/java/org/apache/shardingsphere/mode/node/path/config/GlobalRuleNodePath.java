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

package org.apache.shardingsphere.mode.node.path.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

/**
 * Global props node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalRuleNodePath {
    
    private static final String ROOT_NODE = "/rules";
    
    private static final VersionNodePathParser PARSER = new VersionNodePathParser(getRootPath() + "/" + NodePathPattern.IDENTIFIER);
    
    /**
     * Get global rule root path.
     *
     * @return global rule root path
     */
    public static String getRootPath() {
        return ROOT_NODE;
    }
    
    /**
     * Get global rule path.
     *
     * @param ruleTypeName rule type name
     * @return global rule path
     */
    public static String getRulePath(final String ruleTypeName) {
        return String.join("/", getRootPath(), ruleTypeName);
    }
    
    /**
     * Get global rule version node path generator.
     *
     * @param ruleTypeName rule type name
     * @return global rule version node path generator
     */
    public static VersionNodePathGenerator getVersionNodePathGenerator(final String ruleTypeName) {
        return new VersionNodePathGenerator(getRulePath(ruleTypeName));
    }
    
    /**
     * Get view version pattern node path parser.
     *
     * @return view version node path parser
     */
    public static VersionNodePathParser getVersionNodePathParser() {
        return PARSER;
    }
    
    /**
     * Get rule version node path parser.
     *
     * @param ruleTypeName rule type name
     * @return rule version node path parser
     */
    public static VersionNodePathParser getRuleVersionNodePathParser(final String ruleTypeName) {
        return new VersionNodePathParser(ruleTypeName);
    }
}
