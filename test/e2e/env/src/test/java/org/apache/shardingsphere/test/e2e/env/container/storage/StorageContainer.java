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

package org.apache.shardingsphere.test.e2e.env.container.storage;

import org.apache.shardingsphere.test.e2e.env.container.E2EContainer;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Storage container.
 */
public interface StorageContainer extends E2EContainer {
    
    /**
     * Get actual data source map.
     *
     * @return actual data source map
     */
    Map<String, DataSource> getActualDataSourceMap();
    
    /**
     * Get expected data source map.
     *
     * @return expected data source map
     */
    Map<String, DataSource> getExpectedDataSourceMap();
    
    /**
     * Get link replacements.
     *
     * @return link replacements
     */
    Map<String, String> getLinkReplacements();
}
