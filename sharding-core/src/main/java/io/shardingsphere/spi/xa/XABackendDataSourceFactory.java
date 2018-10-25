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

package io.shardingsphere.spi.xa;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.spi.NewInstanceServiceLoader;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * XA backend datasource factory.
 *
 * @author zhaojun
 */
@Slf4j
public final class XABackendDataSourceFactory implements BackendDataSourceFactory {
    
    private static final NewInstanceServiceLoader<BackendDataSourceFactory> SERVICE_LOADER = NewInstanceServiceLoader.load(BackendDataSourceFactory.class);
    
    private final Collection<BackendDataSourceFactory> backendDataSourceFactories = SERVICE_LOADER.newServiceInstances();
    
    @Override
    public Map<String, DataSource> build(final Map<String, DataSource> dataSourceMap) {
        if (backendDataSourceFactories.isEmpty()) {
            throw new ShardingException("Please make XA DatasourceFactory SPI available.");
        }
        return backendDataSourceFactories.iterator().next().build(dataSourceMap);
    }
}
