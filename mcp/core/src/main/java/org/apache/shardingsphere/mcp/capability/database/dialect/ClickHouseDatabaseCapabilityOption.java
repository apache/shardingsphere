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

package org.apache.shardingsphere.mcp.capability.database.dialect;

import lombok.Getter;
import org.apache.shardingsphere.mcp.capability.database.DatabaseCapabilityOption;
import org.apache.shardingsphere.mcp.capability.database.SchemaSemantics;
import org.apache.shardingsphere.mcp.capability.database.TransactionCapability;

/**
 * Database capability option for ClickHouse.
 */
@Getter
public final class ClickHouseDatabaseCapabilityOption implements DatabaseCapabilityOption {
    
    private final TransactionCapability transactionCapability = TransactionCapability.NONE;
    
    private final boolean indexSupported = false;
    
    private final SchemaSemantics defaultSchemaSemantics = SchemaSemantics.DATABASE_AS_SCHEMA;
    
    private final boolean crossSchemaQuerySupported = false;
    
    @Override
    public boolean isExplainAnalyzeSupported(final String databaseVersion) {
        return false;
    }
    
    @Override
    public String getType() {
        return "ClickHouse";
    }
}
