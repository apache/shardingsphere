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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl;

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.column.DialectColumnOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ParenthesesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Optional;

/**
 * Common projection.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(exclude = "columnBoundInfo")
@ToString
public final class ColumnProjection implements Projection {
    
    private final IdentifierValue owner;
    
    private final IdentifierValue name;
    
    private final IdentifierValue alias;
    
    private final DatabaseType databaseType;
    
    private final ParenthesesSegment leftParentheses;
    
    private final ParenthesesSegment rightParentheses;
    
    private final ColumnSegmentBoundInfo columnBoundInfo;
    
    private String columnLabel;
    
    private String columnName;
    
    public ColumnProjection(final String owner, final String name, final String alias, final DatabaseType databaseType) {
        this(null == owner ? null : new IdentifierValue(owner, QuoteCharacter.NONE), new IdentifierValue(name, QuoteCharacter.NONE),
                null == alias ? null : new IdentifierValue(alias, QuoteCharacter.NONE), databaseType, null, null, null);
    }
    
    public ColumnProjection(final IdentifierValue owner, final IdentifierValue name, final IdentifierValue alias, final DatabaseType databaseType) {
        this(owner, name, alias, databaseType, null, null, null);
    }
    
    public ColumnProjection(final IdentifierValue owner, final IdentifierValue name, final IdentifierValue alias, final DatabaseType databaseType,
                            final ParenthesesSegment leftParentheses, final ParenthesesSegment rightParentheses) {
        this(owner, name, alias, databaseType, leftParentheses, rightParentheses, null);
    }
    
    public ColumnProjection(final IdentifierValue owner, final IdentifierValue name, final IdentifierValue alias, final DatabaseType databaseType, final ParenthesesSegment leftParentheses,
                            final ParenthesesSegment rightParentheses, final ColumnSegmentBoundInfo columnBoundInfo, final boolean initColumnNameAndLabel) {
        this.owner = owner;
        this.name = name;
        this.alias = alias;
        this.databaseType = databaseType;
        this.leftParentheses = leftParentheses;
        this.rightParentheses = rightParentheses;
        this.columnBoundInfo = columnBoundInfo;
        if (initColumnNameAndLabel) {
            columnName = createColumnName(name, databaseType);
            columnLabel = createColumnLabel(name, databaseType);
        }
    }
    
    /**
     * Get owner.
     *
     * @return owner
     */
    public Optional<IdentifierValue> getOwner() {
        return Optional.ofNullable(owner);
    }
    
    /**
     * Get original table.
     *
     * @return original table
     */
    public IdentifierValue getOriginalTable() {
        if (null == columnBoundInfo || null == columnBoundInfo.getOriginalTable() || Strings.isNullOrEmpty(columnBoundInfo.getOriginalTable().getValue())) {
            return null == owner ? new IdentifierValue("") : owner;
        }
        return columnBoundInfo.getOriginalTable();
    }
    
    /**
     * Get original column.
     *
     * @return original column
     */
    public IdentifierValue getOriginalColumn() {
        return null == columnBoundInfo || null == columnBoundInfo.getOriginalColumn() || Strings.isNullOrEmpty(columnBoundInfo.getOriginalColumn().getValue()) ? name
                : columnBoundInfo.getOriginalColumn();
    }
    
    /**
     * Get left parentheses.
     *
     * @return left parentheses
     */
    public Optional<ParenthesesSegment> getLeftParentheses() {
        return Optional.ofNullable(leftParentheses);
    }
    
    /**
     * Get right parentheses.
     *
     * @return right parentheses
     */
    public Optional<ParenthesesSegment> getRightParentheses() {
        return Optional.ofNullable(rightParentheses);
    }
    
    @Override
    public String getColumnName() {
        return null == columnName ? createColumnName(name, databaseType) : columnName;
    }
    
    @Override
    public String getColumnLabel() {
        return null == columnLabel ? createColumnLabel(name, databaseType) : columnLabel;
    }
    
    private String createColumnName(final IdentifierValue name, final DatabaseType databaseType) {
        ProjectionIdentifierExtractEngine extractEngine = new ProjectionIdentifierExtractEngine(databaseType);
        DialectColumnOption columnOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getColumnOption();
        return columnOption.isColumnNameEqualsLabelInColumnProjection() ? getColumnLabel() : extractEngine.getIdentifierValue(name);
    }
    
    private String createColumnLabel(final IdentifierValue name, final DatabaseType databaseType) {
        ProjectionIdentifierExtractEngine extractEngine = new ProjectionIdentifierExtractEngine(databaseType);
        return getAlias().map(extractEngine::getIdentifierValue).orElseGet(() -> extractEngine.getIdentifierValue(name));
    }
    
    @Override
    public String getExpression() {
        return null == owner ? name.getValue() : owner.getValue() + "." + name.getValue();
    }
    
    @Override
    public Optional<IdentifierValue> getAlias() {
        return Optional.ofNullable(alias);
    }
}
