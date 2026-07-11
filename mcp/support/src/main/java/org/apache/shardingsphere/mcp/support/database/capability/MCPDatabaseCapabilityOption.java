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

package org.apache.shardingsphere.mcp.support.database.capability;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

/**
 * MCP database capability option.
 */
@SingletonSPI
public interface MCPDatabaseCapabilityOption extends TypedSPI {
    
    /**
     * Judge whether MCP can execute a single database-native EXPLAIN statement and read its result set.
     *
     * @return whether MCP EXPLAIN execution is supported
     */
    boolean isExplainSupported();
    
    /**
     * Get identifier case policy set.
     *
     * @return identifier case policy set
     */
    default IdentifierCasePolicySet getIdentifierCasePolicySet() {
        return IdentifierCasePolicyFactory.newSensitivePolicySet();
    }
    
    @Override
    String getType();
}
