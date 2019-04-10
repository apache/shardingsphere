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

package org.apache.shardingsphere.shardingjdbc.spring.namespace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Encryptor parser tag constants.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptorRuleBeanDefinitionParserTag {
    
    public static final String ENCRYPTOR_TYPE_ATTRIBUTE = "type";
    
    public static final String ENCRYPTOR_QUALIFIED_COLUMNS_ATTRIBUTE = "qualified-columns";
    
    public static final String ENCRYPTOR_ASSISTED_QUERY_COLUMNS_ATTRIBUTE = "assisted-query-columns";
    
    public static final String ENCRYPTOR_PROPERTY_REF_ATTRIBUTE = "props-ref";
}
