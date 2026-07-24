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

package org.apache.shardingsphere.mcp.support.database.spi;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * MCP feature direct query facade.
 */
public interface MCPFeatureQueryFacade {
    
    /**
     * Query rows from a target database.
     *
     * @param databaseName database name
     * @param sql SQL text
     * @return query rows
     */
    List<Map<String, Object>> query(String databaseName, String sql);
    
    /**
     * Query rows using any configured database.
     *
     * @param sql SQL text
     * @return query rows
     */
    List<Map<String, Object>> queryWithAnyDatabase(String sql);
    
    /**
     * Check whether runtime database capability exists.
     *
     * @param databaseName database name
     * @throws DatabaseCapabilityNotFoundException when database capability does not exist
     */
    void checkDatabaseCapability(String databaseName);
    
    /**
     * Judge whether an identifier references an existing identifier in a runtime database.
     *
     * @param databaseName database name
     * @param identifierScope identifier scope
     * @param identifier identifier
     * @param existingIdentifier existing identifier
     * @return whether the identifiers are the same
     * @throws DatabaseCapabilityNotFoundException when database capability does not exist
     */
    boolean isSameIdentifier(String databaseName, IdentifierScope identifierScope, String identifier, String existingIdentifier);
    
}
