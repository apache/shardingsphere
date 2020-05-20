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
 * Shadow data source parser tag constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowDataSourceBeanDefinitionParserTag {
    
    public static final String ROOT_TAG = "rule";
    
    public static final String MAPPINGS_CONFIG_TAG = "mappings";
    
    public static final String MAPPING_CONFIG_TAG = "mapping";
    
    public static final String COLUMN_CONFIG_TAG = "column";
    
    public static final String PRODUCT_DATA_SOURCE_NAME_ATTRIBUTE = "product-data-source-name";
    
    public static final String SHADOW_DATA_SOURCE_NAME_ATTRIBUTE = "shadow-data-source-name";
}
