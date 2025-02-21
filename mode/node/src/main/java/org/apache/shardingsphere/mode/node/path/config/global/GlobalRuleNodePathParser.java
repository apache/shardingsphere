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

package org.apache.shardingsphere.mode.node.path.config.global;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

/**
 * Global props node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalRuleNodePathParser {
    
    private static final VersionNodePathParser PARSER = new VersionNodePathParser(NodePathGenerator.generatePath(new GlobalRuleNodePath(NodePathPattern.IDENTIFIER), false));
    
    /**
     * Get global rule version node path parser.
     *
     * @return global rule version node path parser
     */
    public static VersionNodePathParser getVersion() {
        return PARSER;
    }
    
    /**
     * Get global rule version node path parser.
     *
     * @param ruleType rule type
     * @return global rule version node path parser
     */
    public static VersionNodePathParser getVersion(final String ruleType) {
        return new VersionNodePathParser(ruleType);
    }
}
