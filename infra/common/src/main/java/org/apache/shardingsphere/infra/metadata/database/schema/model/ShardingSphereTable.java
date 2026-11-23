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

package org.apache.shardingsphere.infra.metadata.database.schema.model;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.metadata.identifier.IdentifierIndex;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * ShardingSphere table.
 */
@Getter
@ToString
public final class ShardingSphereTable {
    
    private final String name;
    
    private final List<ShardingSphereIdentifier> columnNames = new ArrayList<>();
    
    private final List<String> primaryKeyColumns = new ArrayList<>();
    
    private final List<String> visibleColumns = new ArrayList<>();
    
    private final Map<String, Integer> visibleColumnAndIndexMap = new CaseInsensitiveMap<>();
    
    @Getter(AccessLevel.NONE)
    private IdentifierIndex<ShardingSphereColumn> columnIndex;
    
    @Getter(AccessLevel.NONE)
    private IdentifierIndex<ShardingSphereIndex> indexIdentifierIndex;
    
    @Getter(AccessLevel.NONE)
    private IdentifierIndex<ShardingSphereConstraint> constraintIdentifierIndex;
    
    private final TableType type;
    
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private AtomicBoolean bindingsInvalidated = new AtomicBoolean();
    
    /**
     * Construct table with the temporary default identifier context.
     *
     * <p>TODO(haoran): Replace this fallback with explicit identifier context injection after all table creation paths migrate.</p>
     *
     * @param name table name
     * @param columns columns
     * @param indexes indexes
     * @param constraints constraints
     */
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints) {
        this(name, columns, indexes, constraints, TableType.TABLE);
    }
    
    /**
     * Construct table with the temporary default identifier context.
     *
     * <p>TODO(haoran): Replace this fallback with explicit identifier context injection after all table creation paths migrate.</p>
     *
     * @param name table name
     * @param columns columns
     * @param indexes indexes
     * @param constraints constraints
     * @param type table type
     */
    public ShardingSphereTable(final String name, final Collection<ShardingSphereColumn> columns,
                               final Collection<ShardingSphereIndex> indexes, final Collection<ShardingSphereConstraint> constraints, final TableType type) {
        this.name = name;
        DatabaseIdentifierContext identifierContext = DatabaseIdentifierContextFactory.createDefault();
        columnIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.COLUMN);
        indexIdentifierIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.INDEX);
        constraintIdentifierIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.CONSTRAINT);
        this.type = type;
        columnIndex.rebuild(createColumns(columns));
        indexIdentifierIndex.rebuild(createIndexes(indexes));
        constraintIdentifierIndex.rebuild(createConstraints(constraints));
    }
    
    private Map<String, ShardingSphereColumn> createColumns(final Collection<ShardingSphereColumn> columns) {
        Map<String, ShardingSphereColumn> result = new LinkedHashMap<>(columns.size(), 1F);
        int index = 0;
        for (ShardingSphereColumn each : columns) {
            if (result.containsKey(each.getName())) {
                continue;
            }
            result.put(each.getName(), each);
            columnNames.add(new ShardingSphereIdentifier(each.getName()));
            if (each.isPrimaryKey()) {
                primaryKeyColumns.add(each.getName());
            }
            if (each.isVisible()) {
                visibleColumns.add(each.getName());
                visibleColumnAndIndexMap.put(each.getName(), index++);
            }
        }
        return result;
    }
    
    private Map<String, ShardingSphereIndex> createIndexes(final Collection<ShardingSphereIndex> indexes) {
        return indexes.stream()
                .collect(Collectors.toMap(ShardingSphereIndex::getName, each -> each, (oldValue, currentValue) -> currentValue,
                        () -> new LinkedHashMap<>(indexes.size(), 1F)));
    }
    
    private Map<String, ShardingSphereConstraint> createConstraints(final Collection<ShardingSphereConstraint> constraints) {
        return constraints.stream()
                .collect(Collectors.toMap(ShardingSphereConstraint::getName, each -> each, (oldValue, currentValue) -> currentValue,
                        () -> new LinkedHashMap<>(constraints.size(), 1F)));
    }
    
    /**
     * Find column.
     *
     * @param columnName column name
     * @return column
     */
    public Optional<ShardingSphereColumn> findColumn(final IdentifierValue columnName) {
        if (null == columnName || null == columnName.getValue()) {
            return Optional.empty();
        }
        return columnIndex.find(columnName);
    }
    
    /**
     * Judge whether contains column.
     *
     * @param columnName column name
     * @return contains column or not
     */
    public boolean containsColumn(final String columnName) {
        return null != columnName && containsColumn(new IdentifierValue(columnName, QuoteCharacter.NONE));
    }
    
    /**
     * Judge whether contains column.
     *
     * @param columnName column name
     * @return contains column or not
     */
    public boolean containsColumn(final IdentifierValue columnName) {
        return findColumn(columnName).isPresent();
    }
    
    /**
     * Get column.
     *
     * @param columnName column name
     * @return column
     */
    public ShardingSphereColumn getColumn(final String columnName) {
        return null == columnName ? null : getColumn(new IdentifierValue(columnName, QuoteCharacter.NONE));
    }
    
    /**
     * Get column.
     *
     * @param columnName column name
     * @return column
     */
    public ShardingSphereColumn getColumn(final IdentifierValue columnName) {
        return findColumn(columnName).orElse(null);
    }
    
    /**
     * Get all columns.
     *
     * @return columns
     */
    public Collection<ShardingSphereColumn> getAllColumns() {
        return columnIndex.getAll();
    }
    
    /**
     * Find column names If not existed from passing by column names.
     *
     * @param columnNames column names
     * @return found column names
     */
    public Collection<String> findColumnNamesIfNotExistedFrom(final Collection<String> columnNames) {
        if (columnNames.size() == columnIndex.size()) {
            return Collections.emptyList();
        }
        Collection<String> result = new LinkedHashSet<>(columnIndex.getAllNames());
        for (String each : columnNames) {
            ShardingSphereColumn column = getColumn(each);
            if (null != column) {
                result.remove(column.getName());
            }
        }
        return result;
    }
    
    /**
     * Find index.
     *
     * @param indexName index name
     * @return index
     */
    private Optional<ShardingSphereIndex> findIndex(final IdentifierValue indexName) {
        if (null == indexName || null == indexName.getValue()) {
            return Optional.empty();
        }
        return indexIdentifierIndex.find(indexName);
    }
    
    /**
     * Get index.
     *
     * @param indexName index name
     * @return index
     */
    public ShardingSphereIndex getIndex(final String indexName) {
        return null == indexName ? null : getIndex(new IdentifierValue(indexName, QuoteCharacter.NONE));
    }
    
    /**
     * Get index.
     *
     * @param indexName index name
     * @return index
     */
    private ShardingSphereIndex getIndex(final IdentifierValue indexName) {
        return findIndex(indexName).orElse(null);
    }
    
    /**
     * Judge whether contains index.
     *
     * @param indexName index name
     * @return contains index or not
     */
    public boolean containsIndex(final IdentifierValue indexName) {
        return findIndex(indexName).isPresent();
    }
    
    /**
     * Judge whether contains index.
     *
     * @param indexName index name
     * @return contains index or not
     */
    public boolean containsIndex(final String indexName) {
        return null != indexName && containsIndex(new IdentifierValue(indexName, QuoteCharacter.NONE));
    }
    
    /**
     * Find constraint.
     *
     * @param constraintName constraint name
     * @return constraint
     */
    private Optional<ShardingSphereConstraint> findConstraint(final IdentifierValue constraintName) {
        if (null == constraintName || null == constraintName.getValue()) {
            return Optional.empty();
        }
        return constraintIdentifierIndex.find(constraintName);
    }
    
    /**
     * Get constraint.
     *
     * @param constraintName constraint name
     * @return constraint
     */
    public ShardingSphereConstraint getConstraint(final String constraintName) {
        return null == constraintName ? null : getConstraint(new IdentifierValue(constraintName, QuoteCharacter.NONE));
    }
    
    /**
     * Get constraint.
     *
     * @param constraintName constraint name
     * @return constraint
     */
    private ShardingSphereConstraint getConstraint(final IdentifierValue constraintName) {
        return findConstraint(constraintName).orElse(null);
    }
    
    /**
     * Judge whether contains constraint.
     *
     * @param constraintName constraint name
     * @return contains constraint or not
     */
    public boolean containsConstraint(final String constraintName) {
        return null != constraintName && containsConstraint(new IdentifierValue(constraintName, QuoteCharacter.NONE));
    }
    
    /**
     * Judge whether contains constraint.
     *
     * @param constraintName constraint name
     * @return contains constraint or not
     */
    private boolean containsConstraint(final IdentifierValue constraintName) {
        return findConstraint(constraintName).isPresent();
    }
    
    /**
     * Get all indexes.
     *
     * @return indexes
     */
    public Collection<ShardingSphereIndex> getAllIndexes() {
        return indexIdentifierIndex.getAll();
    }
    
    /**
     * Put index.
     *
     * @param index index
     */
    public void putIndex(final ShardingSphereIndex index) {
        indexIdentifierIndex.put(index.getName(), index);
    }
    
    /**
     * Remove index.
     *
     * @param indexName index name
     */
    public void removeIndex(final String indexName) {
        if (null == indexName) {
            return;
        }
        ShardingSphereIndex index = getIndex(indexName);
        if (null == index) {
            return;
        }
        indexIdentifierIndex.remove(index.getName());
    }
    
    /**
     * Get all constraint.
     *
     * @return constraint
     */
    public Collection<ShardingSphereConstraint> getAllConstraints() {
        return constraintIdentifierIndex.getAll();
    }
    
    /**
     * Refresh shared database identifier context.
     *
     * @param identifierContext database identifier context
     */
    public void refreshIdentifierContext(final DatabaseIdentifierContext identifierContext) {
        Collection<ShardingSphereColumn> columns = new LinkedList<>(columnIndex.getAll());
        columnIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.COLUMN);
        columnIndex.rebuild(createColumnMap(columns));
        Collection<ShardingSphereIndex> indexes = new LinkedList<>(indexIdentifierIndex.getAll());
        indexIdentifierIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.INDEX);
        indexIdentifierIndex.rebuild(createIndexes(indexes));
        Collection<ShardingSphereConstraint> constraints = new LinkedList<>(constraintIdentifierIndex.getAll());
        constraintIdentifierIndex = new IdentifierIndex<>(identifierContext, IdentifierScope.CONSTRAINT);
        constraintIdentifierIndex.rebuild(createConstraints(constraints));
    }
    
    private Map<String, ShardingSphereColumn> createColumnMap(final Collection<ShardingSphereColumn> columns) {
        return columns.stream()
                .collect(Collectors.toMap(ShardingSphereColumn::getName, each -> each, (oldValue, currentValue) -> oldValue, () -> new LinkedHashMap<>(columns.size(), 1F)));
    }
    
    /**
     * Determine whether bindings using this table have been invalidated.
     *
     * @return whether a structural change or removal invalidated the bindings
     */
    public boolean isBindingInvalidated() {
        return bindingsInvalidated.get();
    }
    
    void invalidateBindings() {
        bindingsInvalidated.set(true);
    }
    
    void inheritBindings(final ShardingSphereTable previous) {
        if (this == previous) {
            return;
        }
        if (hasSameStructure(previous)) {
            if (!isBindingInvalidated() && !previous.isBindingInvalidated()) {
                bindingsInvalidated = previous.bindingsInvalidated;
            }
        } else {
            previous.invalidateBindings();
        }
    }
    
    private boolean hasSameStructure(final ShardingSphereTable previous) {
        if (!name.equals(previous.name) || type != previous.type) {
            return false;
        }
        return hasSameColumns(previous) && hasSameIndexes(previous) && hasSameConstraints(previous);
    }
    
    private boolean hasSameColumns(final ShardingSphereTable previous) {
        if (getAllColumns().size() != previous.getAllColumns().size()) {
            return false;
        }
        Iterator<ShardingSphereColumn> previousColumns = previous.getAllColumns().iterator();
        for (ShardingSphereColumn each : getAllColumns()) {
            if (!hasSameColumn(each, previousColumns.next())) {
                return false;
            }
        }
        return true;
    }
    
    private boolean hasSameColumn(final ShardingSphereColumn column, final ShardingSphereColumn previous) {
        if (!column.getName().equals(previous.getName()) || column.getDataType() != previous.getDataType()
                || column.isPrimaryKey() != previous.isPrimaryKey() || column.isGenerated() != previous.isGenerated()) {
            return false;
        }
        return column.isCaseSensitive() == previous.isCaseSensitive() && column.isVisible() == previous.isVisible()
                && column.isUnsigned() == previous.isUnsigned() && column.isNullable() == previous.isNullable();
    }
    
    private boolean hasSameIndexes(final ShardingSphereTable previous) {
        return getAllIndexes().size() == previous.getAllIndexes().size() && getAllIndexes().stream().allMatch(each -> hasSameIndex(each, previous.getIndex(each.getName())));
    }
    
    private boolean hasSameIndex(final ShardingSphereIndex index, @Nullable final ShardingSphereIndex previous) {
        return null != previous && index.getName().equals(previous.getName()) && index.isUnique() == previous.isUnique()
                && new ArrayList<>(index.getColumns()).equals(new ArrayList<>(previous.getColumns()));
    }
    
    private boolean hasSameConstraints(final ShardingSphereTable previous) {
        return getAllConstraints().size() == previous.getAllConstraints().size() && getAllConstraints().stream().allMatch(each -> hasSameConstraint(each, previous.getConstraint(each.getName())));
    }
    
    private boolean hasSameConstraint(final ShardingSphereConstraint constraint, @Nullable final ShardingSphereConstraint previous) {
        return null != previous && constraint.getName().equals(previous.getName()) && Objects.equals(constraint.getReferencedTableName(), previous.getReferencedTableName());
    }
    
}
