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

package org.apache.shardingsphere.encrypt.spring.namespace.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Encrypt rule bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptRuleBeanDefinitionTag {
    
    /**
     * Root tag.
     */
    public static final String ROOT_TAG = "rule";
    
    /**
     * Table tag.
     */
    public static final String TABLE_TAG = "table";
    
    /**
     * Column tag.
     */
    public static final String COLUMN_TAG = "column";
    
    /**
     * Logic column attribute.
     */
    public static final String LOGIC_COLUMN_ATTRIBUTE = "logic-column";
    
    /**
     * Cipher column attribute.
     */
    public static final String CIPHER_COLUMN_ATTRIBUTE = "cipher-column";
    
    /**
     * Assisted query column attribute.
     */
    public static final String ASSISTED_QUERY_COLUMN_ATTRIBUTE = "assisted-query-column";
    
    /**
     * Fuzzy query column attribute.
     */
    public static final String FUZZY_QUERY_COLUMN_ATTRIBUTE = "fuzzy-query-column";
    
    /**
     * Plain column attribute.
     */
    public static final String PLAIN_COLUMN_ATTRIBUTE = "plain-column";
    
    /**
     * Encrypt algorithm ref attribute.
     */
    public static final String ENCRYPT_ALGORITHM_REF_ATTRIBUTE = "encrypt-algorithm-ref";
    
    /**
     * Assisted query encrypt algorithm ref attribute.
     */
    public static final String ASSISTED_QUERY_ENCRYPT_ALGORITHM_REF_ATTRIBUTE = "assisted-query-encrypt-algorithm-ref";
    
    /**
     * Fuzzy query encrypt algorithm ref attribute.
     */
    public static final String FUZZY_QUERY_ENCRYPT_ALGORITHM_REF_ATTRIBUTE = "fuzzy-query-encrypt-algorithm-ref";
    
    /**
     * Query with cipher column attribute.
     */
    public static final String QUERY_WITH_CIPHER_COLUMN = "query-with-cipher-column";
}
