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

package org.apache.shardingsphere.infra.aware;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

import java.util.Collection;

/**
 * Data source name aware.
 */
public interface DataSourceNameAware extends TypedSPI {
    
    /**
     * Set rule.
     *
     * @param rule rule
     */
    void setRule(ShardingSphereRule rule);
    
    /**
     * Get primary data source name.
     *
     * @param dataSourceName data source name
     * @return primary data source name
     */
    String getPrimaryDataSourceName(String dataSourceName);
    
    /**
     * Get replica data source names.
     *
     * @param dataSourceName data source name
     * @return replica data source names
     */
    Collection<String> getReplicaDataSourceNames(String dataSourceName);
}
