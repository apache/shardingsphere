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

package org.apache.shardingsphere.mode.metadata.persist.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Global node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalNode {
    
    private static final String RULE_NODE = "rules";
    
    private static final String PROPS_NODE = "props";
    
    /**
     * Get global rule node path.
     *
     * @return global rule node path
     */
    public static String getGlobalRuleNode() {
        return String.join("/", "", RULE_NODE);
    }
    
    /**
     * Get properties path.
     *
     * @return properties path
     */
    public static String getPropsPath() {
        return String.join("/", "", PROPS_NODE);
    }
}
