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
 * Metrics bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetricsBeanDefinitionTag {
    
    public static final String ROOT_TAG = "metrics";
    
    public static final String NAME_TAG = "name";
    
    public static final String HOST_TAG = "host";
    
    public static final String PORT_TAG = "port";
    
    public static final String ASYNC_TAG = "async";
    
    public static final String ENABLE_TAG = "enable";
    
    public static final String THREAD_COUNT_TAG = "thread-count";
    
    public static final String PROPS_TAG = "props";
}
