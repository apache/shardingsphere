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

package org.apache.shardingsphere.readwritesplitting.spring.namespace.tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Readwrite-splitting rule bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingRuleBeanDefinitionTag {
    
    public static final String ROOT_TAG = "rule";
    
    public static final String DATA_SOURCE_TAG = "data-source-rule";
    
    public static final String READWRITE_SPLITTING_DATA_SOURCE_ID_ATTRIBUTE = "id";
    
    public static final String READWRITE_SPLITTING_TYPE = "type";
    
    public static final String READWRITE_SPLITTING_PROPS = "props";
    
    public static final String LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE = "load-balance-algorithm-ref";
}
