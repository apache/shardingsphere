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

package io.shardingsphere.transaction.saga;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * Saga sub transaction.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class SagaSubTransaction {
    
    private final String dataSourceName;
    
    private final String sql;
    
    private final List<List<Object>> parameterSets;
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof SagaSubTransaction && this.toString().equals(obj.toString());
    }
}
