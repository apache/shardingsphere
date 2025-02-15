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

package org.apache.shardingsphere.mode.node.path.metadata.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;

/**
 * Qualified data source node path generator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QualifiedDataSourceNodePathGenerator {
    
    private static final String ROOT_NODE = "/nodes";
    
    private static final String QUALIFIED_DATA_SOURCES_NODE = "qualified_data_sources";
    
    /**
     * Get qualified data source root path.
     *
     * @return qualified data source root path
     */
    public static String getRootPath() {
        return String.join("/", ROOT_NODE, QUALIFIED_DATA_SOURCES_NODE);
    }
    
    /**
     * Get qualified data source path.
     *
     * @param qualifiedDataSource qualified data source
     * @return qualified data source path
     */
    public static String getQualifiedDataSourcePath(final QualifiedDataSource qualifiedDataSource) {
        return String.join("/", getRootPath(), qualifiedDataSource.toString());
    }
}
