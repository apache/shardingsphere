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

package org.apache.shardingsphere.infra.metadata.ddlgenerator.spi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.Optional;

/**
 * Dialect DDL SQL generator factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DialectDDLSQLGeneratorFactory {
    
    static {
        ShardingSphereServiceLoader.register(DialectDDLSQLGenerator.class);
    }
    
    /**
     * Create new instance of dialect DDL SQL generator.
     *
     * @param databaseType database type
     * @return new instance of dialect DDL SQL generator
     */
    public static Optional<DialectDDLSQLGenerator> newInstance(final DatabaseType databaseType) {
        return TypedSPIRegistry.findRegisteredService(DialectDDLSQLGenerator.class, databaseType.getName());
    }
}
