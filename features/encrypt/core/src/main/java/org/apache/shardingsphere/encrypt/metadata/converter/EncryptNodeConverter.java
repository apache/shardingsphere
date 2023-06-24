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

import java.util.regex.Pattern;

/**
 * Encrypt node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptNodeConverter {
    
    public static final String ROOT_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/encrypt";
    
    public static final String RULE_NAME = "/([\\w\\-]+)?";
    
    public static final String RULE_ACTIVE_VERSION = "/([\\w\\-]+)/active_version$";
    
    private static final Pattern ROOT_PATH_PATTERN = Pattern.compile(ROOT_NODE_PREFIX + "\\.*", Pattern.CASE_INSENSITIVE);
    
    private static final EncryptItemNodeConverter TABLE_NODE_CONVERTER = new EncryptItemNodeConverter("tables");
    
    private static final EncryptItemNodeConverter ENCRYPTOR_NODE_CONVERTER = new EncryptItemNodeConverter("encryptors");
    
    /**
     * Is encrypt path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isEncryptPath(final String rulePath) {
        return ROOT_PATH_PATTERN.matcher(rulePath).find();
    }
    
    /**
     * Get table node convertor.
     *
     * @return table node convertor
     */
    public static EncryptItemNodeConverter getTableNodeConvertor() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get encryptor node convertor.
     *
     * @return encryptor node convertor
     */
    public static EncryptItemNodeConverter getEncryptorNodeConvertor() {
        return ENCRYPTOR_NODE_CONVERTER;
    }
}
