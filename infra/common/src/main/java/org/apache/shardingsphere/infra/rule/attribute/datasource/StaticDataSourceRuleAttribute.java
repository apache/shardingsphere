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

package org.apache.shardingsphere.infra.rule.attribute.datasource;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttribute;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;

/**
 * Static data source rule attribute.
 */
public interface StaticDataSourceRuleAttribute extends RuleAttribute {
    
    /**
     * Update data source status.
     *
     * @param qualifiedDataSource qualified data source
     * @param status data source state
     */
    void updateStatus(QualifiedDataSource qualifiedDataSource, DataSourceState status);
    
    /**
     * Clean single storage node data source.
     *
     * @param groupName group name
     */
    void cleanStorageNodeDataSource(String groupName);
    
    /**
     * Clean storage nodes data sources.
     */
    void cleanStorageNodeDataSources();
}
