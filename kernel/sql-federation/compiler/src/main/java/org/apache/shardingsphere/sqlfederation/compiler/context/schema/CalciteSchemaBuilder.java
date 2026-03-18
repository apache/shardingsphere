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

package org.apache.shardingsphere.sqlfederation.compiler.context.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.lookup.LikePattern;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationDatabase;
import org.apache.shardingsphere.sqlfederation.compiler.metadata.schema.SQLFederationSchema;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.DialectSQLFederationFunctionRegister;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Calcite schema builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CalciteSchemaBuilder {
    
    /**
     * Build.
     *
     * @param databases databases
     * @return calcite schema
     */
    public static CalciteSchema build(final Collection<ShardingSphereDatabase> databases) {
        CalciteSchema result = CalciteSchema.createRootSchema(true);
        for (ShardingSphereDatabase each : databases) {
            if (each.getAllSchemas().isEmpty()) {
                continue;
            }
            Optional<String> defaultSchema = new DatabaseTypeRegistry(each.getProtocolType()).getDialectDatabaseMetaData().getSchemaOption().getDefaultSchema();
            AbstractSchema schema = defaultSchema.isPresent() ? buildDatabase(each) : buildSchema(each.getAllSchemas().iterator().next(), each.getProtocolType());
            result.add(each.getName(), schema);
        }
        registerFunction(databases, result);
        return result;
    }
    
    private static AbstractSchema buildDatabase(final ShardingSphereDatabase database) {
        return new SQLFederationDatabase(database, database.getProtocolType());
    }
    
    private static AbstractSchema buildSchema(final ShardingSphereSchema schema, final DatabaseType protocolType) {
        return new SQLFederationSchema(schema.getName(), schema, protocolType);
    }
    
    private static void registerFunction(final Collection<ShardingSphereDatabase> databases, final CalciteSchema calciteSchema) {
        DatabaseType databaseType = databases.isEmpty() ? DatabaseTypeEngine.getDefaultStorageType() : databases.iterator().next().getProtocolType();
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData();
        if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
            registerNestedSchemaFunction(calciteSchema, databaseType);
        } else {
            registerFunction(calciteSchema, databaseType);
        }
    }
    
    private static void registerFunction(final CalciteSchema calciteSchema, final DatabaseType databaseType) {
        for (CalciteSchema each : calciteSchema.getSubSchemaMap().values()) {
            DatabaseTypedSPILoader.findService(DialectSQLFederationFunctionRegister.class, databaseType).ifPresent(optional -> optional.registerFunction(each.plus(), each.getName()));
        }
    }
    
    private static void registerFunction(final Collection<CalciteSchema> subSchemas, final DatabaseType databaseType) {
        for (CalciteSchema each : subSchemas) {
            DatabaseTypedSPILoader.findService(DialectSQLFederationFunctionRegister.class, databaseType).ifPresent(optional -> optional.registerFunction(each.plus(), each.getName()));
        }
    }
    
    private static void registerNestedSchemaFunction(final CalciteSchema calciteSchema, final DatabaseType databaseType) {
        for (CalciteSchema each : calciteSchema.getSubSchemaMap().values()) {
            registerFunction(each.subSchemas().getNames(LikePattern.any()).stream().map(schemaName -> each.subSchemas().get(schemaName)).collect(Collectors.toList()), databaseType);
        }
    }
}
