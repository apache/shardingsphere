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

package org.apache.shardingsphere.mcp.capability;

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * Database capability builder.
 */
@SingletonSPI
public interface DatabaseCapabilityBuilder extends TypedSPI {
    
    /**
     * Get database type.
     *
     * @return database type
     */
    String getDatabaseType();
    
    /**
     * Get transaction capability.
     *
     * @return transaction capability
     */
    TransactionCapability getTransactionCapability();
    
    /**
     * Judge whether index metadata is supported.
     *
     * @return whether index metadata is supported
     */
    boolean isIndexSupported();
    
    /**
     * Get default schema semantics.
     *
     * @return default schema semantics
     */
    SchemaSemantics getDefaultSchemaSemantics();
    
    /**
     * Judge whether cross-schema query is supported.
     *
     * @return whether cross-schema query is supported
     */
    boolean isCrossSchemaQuerySupported();
    
    /**
     * Judge whether explain analyze is supported.
     *
     * @return whether explain analyze is supported
     */
    boolean isExplainAnalyzeSupported();
    
    /**
     * Judge whether explain analyze is supported for database version.
     *
     * @param databaseVersion database version
     * @return whether explain analyze is supported
     */
    default boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return isExplainAnalyzeSupported();
    }
    
    /**
     * Build database capability.
     *
     * @param databaseName logical database name
     * @param databaseVersion database version
     * @return database capability
     */
    default DatabaseCapability build(final String databaseName, final String databaseVersion) {
        return DatabaseCapabilityBuilderSupport.createDefaultCapability(
                databaseName, getDatabaseType(), getTransactionCapability(), isIndexSupported(), getDefaultSchemaSemantics(),
                isCrossSchemaQuerySupported(), isExplainAnalyzeSupported(databaseVersion));
    }
    
    @Override
    default String getType() {
        return getDatabaseType();
    }
}
