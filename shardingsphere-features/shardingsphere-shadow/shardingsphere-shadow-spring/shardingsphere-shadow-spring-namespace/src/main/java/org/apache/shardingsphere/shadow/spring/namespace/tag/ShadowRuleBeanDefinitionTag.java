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

package org.apache.shardingsphere.shadow.spring.namespace.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Shadow rule bean definition tag constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowRuleBeanDefinitionTag {
    
    public static final String ROOT_TAG = "rule";
    
    public static final String DATA_SOURCE_TAG = "data-source";
    
    public static final String DATA_SOURCE_ID_ATTRIBUTE = "id";
    
    public static final String SHADOW_NAME_ATTRIBUTE = "name";
    
    public static final String PRODUCTION_DATA_SOURCE_NAME_ATTRIBUTE = "production-data-source-name";
    
    public static final String SHADOW_DATA_SOURCE_NAME_ATTRIBUTE = "shadow-data-source-name";
    
    public static final String SHADOW_TABLE_TAG = "shadow-table";
    
    public static final String SHADOW_DEFAULT_SHADOW_ALGORITHM_NAME = "default-shadow-algorithm-name";
    
    public static final String SHADOW_TABLE_DATA_SOURCE_REFS_ATTRIBUTE = "data-sources";
    
    public static final String SHADOW_TABLE_ALGORITHM_TAG = "algorithm";
    
    public static final String SHADOW_TABLE_ALGORITHM_REF_ATTRIBUTE = "shadow-algorithm-ref";
}
