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

package org.apache.shardingsphere.spring.namespace.orchestration.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Data source bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceBeanDefinitionTag {
    
    public static final String ROOT_TAG = "data-source";
    
    public static final String ID_ATTRIBUTE = "id";
    
    public static final String DATA_SOURCE_NAMES_TAG = "data-source-names";
    
    public static final String RULE_REFS_TAG = "rule-refs";
    
    public static final String PROPS_TAG = "props";
    
    public static final String REG_CENTER_REF_ATTRIBUTE = "reg-center-ref";
    
    public static final String CONFIG_CENTER_REF_ATTRIBUTE = "config-center-ref";
    
    public static final String METRICS_REF_ATTRIBUTE = "metrics-ref";
    
    public static final String OVERWRITE_ATTRIBUTE = "overwrite";
}
