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

package org.apache.shardingsphere.infra.lock;

import com.google.common.base.Joiner;

/**
 * Lock name util.
 */
public final class LockNameUtil {
    
    private static final String METADATA_REFRESH_LOCK_NAME = "metadata_refresh";
    
    /**
     * Get table lock name.
     * 
     * @param schemaName schema name
     * @param tableName  table name
     * @return table lock name
     */
    public static String getTableLockName(final String schemaName, final String tableName) {
        return Joiner.on(".").join(schemaName, tableName);
    }
    
    /**
     * Get metadata refresh lock name.
     * 
     * @return metadata refresh lock name
     */
    public static String getMetadataRefreshLockName() {
        return METADATA_REFRESH_LOCK_NAME;
    }
}
