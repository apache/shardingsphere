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

package org.apache.shardingsphere.test.e2e.operation.pipeline.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class DataSourceTestUtils {
    
    /**
     * Create standard data source.
     *
     * @param url URL
     * @param username username
     * @param password password
     * @return standard data source
     */
    public static PipelineDataSource createStandardDataSource(final String url, final String username, final String password) {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", url);
        poolProps.put("username", username);
        poolProps.put("password", password);
        return new PipelineDataSource(new StandardPipelineDataSourceConfiguration(poolProps));
    }
}
