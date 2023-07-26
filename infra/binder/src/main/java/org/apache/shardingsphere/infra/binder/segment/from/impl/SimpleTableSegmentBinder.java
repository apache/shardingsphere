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

package org.apache.shardingsphere.infra.binder.segment.from.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Simple table segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTableSegmentBinder {
    
    /**
     * Bind simple table segment with metadata.
     *
     * @param segment simple table segment
     * @param defaultDatabaseName default database name
     * @param databaseType database type
     * @return bounded simple table segment
     */
    public static SimpleTableSegment bind(final SimpleTableSegment segment, final String defaultDatabaseName, DatabaseType databaseType) {
        segment.getTableName().setOriginalDatabase(getDatabaseName(segment, databaseType, defaultDatabaseName));
        segment.getTableName().setOriginalSchema(segment.getOwner().map(OwnerSegment::getIdentifier)
                .orElseGet(() -> new IdentifierValue(DatabaseTypeEngine.getDefaultSchemaName(databaseType, defaultDatabaseName))));
        return segment;
    }
    
    private static IdentifierValue getDatabaseName(final SimpleTableSegment tableSegment, final DatabaseType databaseType, String defaultDatabaseName) {
        Optional<OwnerSegment> owner = databaseType.getDefaultSchema().isPresent() ? tableSegment.getOwner().flatMap(OwnerSegment::getOwner) : tableSegment.getOwner();
        return new IdentifierValue(owner.map(optional -> optional.getIdentifier().getValue()).orElse(defaultDatabaseName));
    }
}
