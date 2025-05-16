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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type;

import com.cedarsoftware.util.CaseInsensitiveMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

/**
 * Simple table segment binder context.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SimpleTableSegmentBinderContext implements TableSegmentBinderContext {
    
    private static final String PG_CATALOG = "pg_catalog";
    
    @Getter(AccessLevel.NONE)
    private final Map<String, ProjectionSegment> columnLabelProjectionSegments;
    
    private final TableSourceType tableSourceType;
    
    private boolean fromWithSegment;
    
    public SimpleTableSegmentBinderContext(final Collection<ProjectionSegment> projectionSegments, final TableSourceType tableSourceType) {
        columnLabelProjectionSegments = new CaseInsensitiveMap<>(projectionSegments.size(), 1F);
        projectionSegments.forEach(each -> putColumnLabelProjectionSegments(each, columnLabelProjectionSegments));
        this.tableSourceType = tableSourceType;
    }
    
    private void putColumnLabelProjectionSegments(final ProjectionSegment projectionSegment, final Map<String, ProjectionSegment> columnLabelProjectionSegments) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            ((ShorthandProjectionSegment) projectionSegment).getActualProjectionSegments().forEach(each -> columnLabelProjectionSegments.put(each.getColumnLabel(), each));
        } else {
            columnLabelProjectionSegments.put(projectionSegment.getColumnLabel(), projectionSegment);
        }
    }
    
    @Override
    public Optional<ProjectionSegment> findProjectionSegmentByColumnLabel(final String columnLabel) {
        return Optional.ofNullable(columnLabelProjectionSegments.get(columnLabel));
    }
    
    @Override
    public Collection<ProjectionSegment> getProjectionSegments() {
        return columnLabelProjectionSegments.values();
    }
    
    /**
     * Get schema name.
     *
     * @param segment simple table segment
     * @param binderContext statement binder context
     * @return schema identifier value
     */
    public static IdentifierValue getSchemaName(final SimpleTableSegment segment, final SQLStatementBinderContext binderContext) {
        if (segment.getOwner().isPresent()) {
            return segment.getOwner().get().getIdentifier();
        }
        // TODO getSchemaName according to search path
        DatabaseType databaseType = binderContext.getSqlStatement().getDatabaseType();
        if ((databaseType instanceof PostgreSQLDatabaseType || databaseType instanceof OpenGaussDatabaseType)
                && SystemSchemaManager.isSystemTable(databaseType.getType(), PG_CATALOG, segment.getTableName().getIdentifier().getValue())) {
            return new IdentifierValue(PG_CATALOG);
        }
        return new IdentifierValue(new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(binderContext.getCurrentDatabaseName()));
    }
}
