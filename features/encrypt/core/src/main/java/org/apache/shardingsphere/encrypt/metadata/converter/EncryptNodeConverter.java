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
import org.apache.shardingsphere.infra.metadata.converter.RuleItemNodeConverter;
import org.apache.shardingsphere.infra.metadata.converter.RuleRootNodeConverter;

/**
 * Encrypt node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptNodeConverter {
    
    private static final RuleRootNodeConverter ROOT_NODE_CONVERTER = new RuleRootNodeConverter("encrypt");
    
    private static final RuleItemNodeConverter TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "tables");
    
    private static final RuleItemNodeConverter ENCRYPTOR_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "encryptors");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodeConverter getRuleRootNodeConverter() {
        return ROOT_NODE_CONVERTER;
    }
    
    /**
     * Get table node convertor.
     *
     * @return table node convertor
     */
    public static RuleItemNodeConverter getTableNodeConvertor() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get encryptor node convertor.
     *
     * @return encryptor node convertor
     */
    public static RuleItemNodeConverter getEncryptorNodeConvertor() {
        return ENCRYPTOR_NODE_CONVERTER;
    }
}
