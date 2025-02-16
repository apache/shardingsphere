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
import org.apache.shardingsphere.mode.node.path.NodePathVersion;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;

/**
 * Storage unit node path generator.
 */
@RequiredArgsConstructor
public final class StorageUnitNodePath implements NodePathVersion<String> {
    
    private static final String UNITS_NODE = "units";
    
    private final String databaseName;
    
    @Override
    public String getRootPath() {
        return String.join("/", new DataSourceNodePath(databaseName).getRootPath(), UNITS_NODE);
    }
    
    @Override
    public String getPath(final String node) {
        return String.join("/", getRootPath(), node);
    }
    
    @Override
    public VersionNodePathGenerator getVersion(final String node) {
        return new VersionNodePathGenerator(getPath(node));
    }
}
