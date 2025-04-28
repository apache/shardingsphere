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

package org.apache.shardingsphere.infra.database.opengauss.metadata.database.option;

import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.schema.DialectSchemaOption;

import java.sql.Connection;
import java.util.Optional;

/**
 * Schema option for openGauss.
 */
public final class OpenGaussSchemaOption implements DialectSchemaOption {
    
    private final DialectSchemaOption defaultSchemaOption = new DefaultSchemaOption(true, "public");
    
    @Override
    public boolean isSchemaAvailable() {
        return defaultSchemaOption.isSchemaAvailable();
    }
    
    @Override
    public String getSchema(final Connection connection) {
        return defaultSchemaOption.getSchema(connection);
    }
    
    @Override
    public Optional<String> getDefaultSchema() {
        return defaultSchemaOption.getDefaultSchema();
    }
    
    @Override
    public Optional<String> getDefaultSystemSchema() {
        return Optional.of("pg_catalog");
    }
}
