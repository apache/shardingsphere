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
 * Orchestration instance bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstanceBeanDefinitionTag {
    
    public static final String ROOT_TAG = "instance";
    
    public static final String TYPE_TAG = "instance-type";
    
    public static final String SERVER_LISTS_TAG = "server-lists";
    
    public static final String NAMESPACE_TAG = "namespace";
    
    public static final String PROP_TAG = "props";
}
