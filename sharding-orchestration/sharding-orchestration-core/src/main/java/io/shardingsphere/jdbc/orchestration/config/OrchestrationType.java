/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.config;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Orchestration type.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public enum OrchestrationType {
    
    SHARDING("SHARDING"), MASTER_SLAVE("MASTER_SLAVE");
    
    private final String typeName;
    
    /**
     * Get orchestration type enum via type name string.
     *
     * @param typeName type name string
     * @return orchestration enum
     */
    public static OrchestrationType valueFrom(final String typeName) {
        Optional<OrchestrationType> typeOptional = Iterators.tryFind(Arrays.asList(OrchestrationType.values()).iterator(), new Predicate<OrchestrationType>() {
            
            @Override
            public boolean apply(final OrchestrationType input) {
                return input.typeName.equals(typeName.toUpperCase());
            }
        });
        if (typeOptional.isPresent()) {
            return typeOptional.get();
        }
        throw new UnsupportedOperationException(String.format("Can not support orchestration type [%s].", typeName));
    }
}
