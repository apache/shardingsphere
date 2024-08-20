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

package org.apache.shardingsphere.infra.database.core.type;

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.Optional;

/**
 * Database type.
 */
@SingletonSPI
public interface DatabaseType extends TypedSPI {
    
    /**
     * Get JDBC URL prefixes.
     *
     * @return prefixes of JDBC URL
     */
    Collection<String> getJdbcUrlPrefixes();
    
    /**
     * Get trunk database type.
     *
     * @return trunk database type
     */
    default Optional<DatabaseType> getTrunkDatabaseType() {
        return Optional.empty();
    }
    
    /**
     * Judge whether current database type is instance of trunk database type.
     *
     * @param databaseTypeClass database type class
     * @return true if current database type is instance of trunk database type, otherwise false
     */
    default boolean isSubtypeOfTrunkDatabase(Class<? extends DatabaseType> databaseTypeClass) {
        return databaseTypeClass.isInstance(this) || getTrunkDatabaseType().map(databaseType -> databaseType.isSubtypeOfTrunkDatabase(databaseTypeClass)).orElse(false);
    }
    
    @Override
    String getType();
}
