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

package io.shardingsphere.core.spi.connection.close;

import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;

/**
 * Connection hook.
 *
 * @author zhangliang
 */
public interface CloseConnectionHook {
    
    /**
     * Handle when close connection started.
     * 
     * @param dataSourceName data source name
     * @param dataSourceMetaData data source meta data
     */
    void start(String dataSourceName, DataSourceMetaData dataSourceMetaData);
    
    /**
     * Handle when close connection finished success.
     */
    void finishSuccess();
    
    /**
     * Handle when close connection finished failure.
     * 
     * @param cause failure cause
     */
    void finishFailure(Exception cause);
}
