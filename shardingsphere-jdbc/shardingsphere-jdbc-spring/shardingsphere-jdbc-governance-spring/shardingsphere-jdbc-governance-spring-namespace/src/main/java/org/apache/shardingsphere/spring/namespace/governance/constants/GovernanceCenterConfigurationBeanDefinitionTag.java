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

package org.apache.shardingsphere.spring.namespace.governance.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Governance center configuration bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GovernanceCenterConfigurationBeanDefinitionTag {
    
    public static final String REG_CENTER_ROOT_TAG = "reg-center";
    
    public static final String CONFIG_CENTER_ROOT_TAG = "config-center";
    
    public static final String TYPE_ATTRIBUTE = "type";
    
    public static final String SERVER_LISTS_ATTRIBUTE = "server-lists";
    
    public static final String PROP_TAG = "props";
}
