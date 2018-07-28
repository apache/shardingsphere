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

package io.shardingsphere.proxy.config;

import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Proxy raw data source.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public abstract class ProxyRawDataSource {
    
    private final Map<String, DataSourceParameter> dataSourceParameters;
    
    /**
     * Build data source map.
     *
     * @return data source map
     */
    public final Map<String, DataSource> build() {
        Map<String, DataSource> result = new LinkedHashMap<>(128, 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), buildInternal(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    protected abstract DataSource buildInternal(String dataSourceName, DataSourceParameter dataSourceParameter);
}
