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

package org.apache.shardingsphere.sharding.spring.namespace.tag.strategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding audit strategy bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingAuditStrategyBeanDefinitionTag {
    
    public static final String ROOT_TAG = "audit-strategy";
    
    public static final String AUDITORS_TAG = "auditors";
    
    public static final String AUDITOR_TAG = "auditor";
    
    public static final String ALLOW_HINT_DISABLE_ATTRIBUTE = "allow-hint-disable";
    
    public static final String ALGORITHM_REF_ATTRIBUTE = "algorithm-ref";
}
