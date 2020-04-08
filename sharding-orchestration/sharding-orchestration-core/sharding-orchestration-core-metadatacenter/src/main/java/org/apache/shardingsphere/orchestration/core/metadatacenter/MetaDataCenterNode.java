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

package org.apache.shardingsphere.orchestration.core.metadatacenter;

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;

/**
 * MetaData node.
 */
@RequiredArgsConstructor
public final class MetaDataCenterNode {
    
    private static final String ROOT = "metadatas";
    
    private final String name;
    
    /**
     * Get metadata node full path.
     *
     * @param schemaName schema name
     * @return metadata node full path
     */
    public String getMetaDataCenterNodeFullPath(final String schemaName) {
        return Joiner.on("/").join("", name, ROOT, schemaName);
    }
}
