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

package org.apache.shardingsphere.infra.metadata.schema.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.Collection;

/**
 * System schema utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemSchemaUtil {
    
    /**
     * Judge whether sql statement contains system schema or not.
     * 
     * @param databaseType databaseType
     * @param schemaNames schema names
     * @param sessionSchemaName session schema name
     * @return whether sql statement contains system schema or not
     */
    public static boolean containsSystemSchema(final DatabaseType databaseType, final Collection<String> schemaNames, final String sessionSchemaName) {
        for (String each : schemaNames) {
            if (!databaseType.getSystemSchemas().contains(each)) {
                continue;
            }
            return true;
        }
        return databaseType.getSystemSchemas().contains(sessionSchemaName);
    }
}
