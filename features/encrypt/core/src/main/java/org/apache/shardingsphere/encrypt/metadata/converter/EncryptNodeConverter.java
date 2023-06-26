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

package org.apache.shardingsphere.encrypt.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.nodepath.item.NamedRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleRootNodePath;

/**
 * Encrypt node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptNodeConverter {
    
    private static final RuleRootNodePath ROOT_NODE_PATH = new RuleRootNodePath("encrypt");
    
    private static final NamedRuleItemNodePath TABLE_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "tables");
    
    private static final NamedRuleItemNodePath ENCRYPTOR_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "encryptors");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodePath getRuleRootNodePath() {
        return ROOT_NODE_PATH;
    }
    
    /**
     * Get table node path.
     *
     * @return table node path
     */
    public static NamedRuleItemNodePath getTableNodePath() {
        return TABLE_NODE_PATH;
    }
    
    /**
     * Get encryptor node path.
     *
     * @return encryptor node path
     */
    public static NamedRuleItemNodePath getEncryptorNodePath() {
        return ENCRYPTOR_NODE_PATH;
    }
}
