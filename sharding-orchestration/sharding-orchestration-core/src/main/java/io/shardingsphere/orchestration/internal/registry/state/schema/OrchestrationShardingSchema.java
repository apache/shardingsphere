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

package io.shardingsphere.orchestration.internal.registry.state.schema;

import com.google.common.base.Splitter;
import io.shardingsphere.core.constant.ShardingConstant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Orchestration sharding schema.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class OrchestrationShardingSchema {
    
    private final String schemaName;
    
    private final String dataSourceName;
    
    public OrchestrationShardingSchema(final String value) {
        if (value.contains(".")) {
            List<String> values = Splitter.on(".").splitToList(value);
            schemaName = values.get(0);
            dataSourceName = values.get(1);
        } else {
            schemaName = ShardingConstant.LOGIC_SCHEMA_NAME;
            dataSourceName = value;
        }
    }
}
