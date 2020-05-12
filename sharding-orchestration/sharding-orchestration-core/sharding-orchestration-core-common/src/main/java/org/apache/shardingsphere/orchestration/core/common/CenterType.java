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

package org.apache.shardingsphere.orchestration.core.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.orchestration.center.exception.OrchestrationException;

import java.util.Arrays;

/**
 * Orchestration type.
 */
@Getter
@RequiredArgsConstructor
public enum CenterType {
    
    REGISTRY_CENTER("registry_center"),
    CONFIG_CENTER("config_center"),
    METADATA_CENTER("metadata_center");
    
    private final String value;
    
    /**
     * Find OrchestrationType via property value.
     *
     * @param value property value
     * @return OrchestrationType enum, return {@code null} if not found
     */
    public static CenterType findByValue(final String value) {
        return Arrays.stream(CenterType.values())
                .filter(each -> each.getValue().equals(value)).findFirst()
                .orElseThrow(() -> new OrchestrationException("now only support :{}, {}, {}",
                        CenterType.CONFIG_CENTER.getValue(), CenterType.REGISTRY_CENTER.getValue(), CenterType.METADATA_CENTER));
    }
}
