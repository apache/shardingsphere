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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Default schema option.
 */
public final class DefaultSchemaOption implements DialectSchemaOption {
    
    private final boolean schemaAvailable;
    
    private final String defaultSchema;
    
    private final DialectSchemaSemantics schemaSemantics;
    
    public DefaultSchemaOption(final boolean schemaAvailable, final String defaultSchema, final DialectSchemaSemantics schemaSemantics) {
        this.schemaAvailable = schemaAvailable;
        this.defaultSchema = defaultSchema;
        this.schemaSemantics = schemaSemantics;
    }
    
    @Override
    public boolean isSchemaAvailable() {
        return schemaAvailable;
    }
    
    @Override
    @SuppressWarnings("ReturnOfNull")
    public String getSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    @Override
    public Optional<String> getDefaultSchema() {
        return Optional.ofNullable(defaultSchema);
    }
    
    @Override
    public Optional<String> getDefaultSystemSchema() {
        return Optional.empty();
    }
    
    @Override
    public DialectSchemaSemantics getSchemaSemantics() {
        return schemaSemantics;
    }
}
