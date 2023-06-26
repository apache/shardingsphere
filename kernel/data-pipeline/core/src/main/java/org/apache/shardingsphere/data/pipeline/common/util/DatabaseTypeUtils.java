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

package org.apache.shardingsphere.data.pipeline.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.BranchDatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * Database type utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypeUtils {
    
    /**
     * Get trunk and branch database types.
     *
     * @param trunkDatabaseTypes trunk database types
     * @return database types
     */
    public static Collection<String> getTrunkAndBranchDatabaseTypes(final Set<String> trunkDatabaseTypes) {
        Collection<String> result = new LinkedList<>();
        for (DatabaseType each : ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class)) {
            if (trunkDatabaseTypes.contains(each.getType())
                    || each instanceof BranchDatabaseType && trunkDatabaseTypes.contains(((BranchDatabaseType) each).getTrunkDatabaseType().getType())) {
                result.add(each.getType());
            }
        }
        return result;
    }
}
