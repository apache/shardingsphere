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

package org.apache.shardingsphere.infra.metadata.schema.builder.loader.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Table meta data loader utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableMetaDataLoaderUtil {
    
    /**
     * Get logic index name.
     * 
     * @param actualIndexName actual index name
     * @param actualTableName actual table name
     * @return logic index
     */
    public static String getLogicIndexName(final String actualIndexName, final String actualTableName) {
        String indexNameSuffix = "_" + actualTableName;
        return actualIndexName.endsWith(indexNameSuffix) ? actualIndexName.replace(indexNameSuffix, "") : actualIndexName;
    }
}
