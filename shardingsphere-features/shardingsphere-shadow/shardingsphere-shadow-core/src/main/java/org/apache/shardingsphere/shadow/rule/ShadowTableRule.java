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

package org.apache.shardingsphere.shadow.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Shadow table rule.
 */
@RequiredArgsConstructor
@Getter
public final class ShadowTableRule {
    
    private final String tableName;
    
    private final Collection<String> shadowDataSources;
    
    private final Collection<String> shadowAlgorithmNames;
    
    /**
     * Shadow table rule.
     * FIXME removed after shadow distsql repaired.
     *
     * @param tableName table name
     * @param shadowAlgorithmNames shadow algorithm names
     * @deprecated removed after shadow distsql repaired
     */
    @Deprecated
    public ShadowTableRule(final String tableName, final Collection<String> shadowAlgorithmNames) {
        this.tableName = tableName;
        this.shadowDataSources = new LinkedList<>();
        this.shadowAlgorithmNames = shadowAlgorithmNames;
    }
}
