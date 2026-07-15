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

import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Database dialect SQL exception classifier for MCP.
 */
@SingletonSPI
public interface MCPDialectSQLExceptionClassifier extends TypedSPI {
    
    /**
     * Classify SQL exception when JDBC-standard evidence is ambiguous.
     *
     * @param cause SQL exception
     * @return classified category
     */
    Optional<MCPJDBCErrorCategory> classify(SQLException cause);
    
    @Override
    String getType();
}
