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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Map;
import java.util.Optional;

/**
 * Create table token.
 */
public final class CreateTableToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final IdentifierValue tableName;
    
    private final Map<String, StorageUnit> storageUnits;
    
    public CreateTableToken(final int startIndex, final int stopIndex, final IdentifierValue tableName, final ShardingSphereDatabase database) {
        super(startIndex, 50);
        this.stopIndex = stopIndex;
        this.tableName = tableName;
        storageUnits = database.getResourceMetaData().getStorageUnits();
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        String actualDataSourceName = routeUnit.getDataSourceMapper().getActualName();
        StorageUnit routeStorageUnit = storageUnits.get(actualDataSourceName);
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(routeStorageUnit.getStorageType());
        if (databaseTypeRegistry.getDialectDatabaseMetaData().getConnectionOption().isInstanceConnectionAvailable()) {
            return getValueWithQuoteCharacters(routeStorageUnit, databaseTypeRegistry);
        }
        return tableName.getValueWithQuoteCharacters();
    }
    
    private String getValueWithQuoteCharacters(final StorageUnit storageUnit, final DatabaseTypeRegistry databaseTypeRegistry) {
        String schema = Optional.ofNullable(storageUnit.getConnectionProperties().getSchema()).orElseGet(() -> storageUnit.getConnectionProperties().getCatalog());
        String warpedSchema = databaseTypeRegistry.getDialectDatabaseMetaData().getQuoteCharacter().wrap(databaseTypeRegistry.formatIdentifierPattern(schema));
        return warpedSchema + "." + tableName.getValueWithQuoteCharacters();
    }
}
