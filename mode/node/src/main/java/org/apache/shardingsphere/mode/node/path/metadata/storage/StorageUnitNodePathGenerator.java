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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Storage unit node path generator.
 */
@RequiredArgsConstructor
public final class StorageUnitNodePathGenerator implements NodePathGenerator<String> {
    
    private static final String UNITS_NODE = "units";
    
    private final String databaseName;
    
    @Override
    public String getRootPath() {
        return String.join("/", new DataSourceNodePathGenerator(databaseName).getRootPath(), UNITS_NODE);
    }
    
    @Override
    public String getPath(final String storageUnitName) {
        return String.join("/", getRootPath(), storageUnitName);
    }
    
    /**
     * Get storage unit version node path generator.
     *
     * @param storageUnitName storage unit name
     * @return storage unit version node path generator
     */
    public VersionNodePathGenerator getVersion(final String storageUnitName) {
        return new VersionNodePathGenerator(String.join("/", getRootPath(), storageUnitName));
    }
}
