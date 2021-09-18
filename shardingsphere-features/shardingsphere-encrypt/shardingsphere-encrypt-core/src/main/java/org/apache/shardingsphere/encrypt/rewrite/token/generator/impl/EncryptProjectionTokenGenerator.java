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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.BaseEncryptSQLTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.SubstitutableColumnNameToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;

import lombok.Setter;

/**
 * Projection token generator for encrypt.
 */
@Setter
public final class EncryptProjectionTokenGenerator extends BaseEncryptSQLTokenGenerator 
        implements CollectionSQLTokenGenerator<SelectStatementContext>, QueryWithCipherColumnAware, PreviousSQLTokensAware {
    
    private boolean queryWithCipherColumn;
    
    private List<SQLToken> previousSQLTokens;
    
    @Override
    protected boolean isGenerateSQLTokenForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getAllTables().isEmpty();
    }
    
    @Override
    public Collection<SubstitutableColumnNameToken> generateSQLTokens(final SelectStatementContext selectStatementContext) {
        ProjectionsSegment projectionsSegment = selectStatementContext.getSqlStatement().getProjections();
        Collection<SimpleTableSegment> simpleTableSegments = selectStatementContext.getAllTables();
        Map<String, SimpleTableSegment> tableSegmentMap = selectStatementContext.getTablesContext().getUniqueTables();
        List<SubstitutableColumnNameToken> substitutableColumnNameTokenList = new ArrayList<>();
        for (SimpleTableSegment simpleTableSegment : tableSegmentMap.values()) {
            String tableName = simpleTableSegment.getTableName().getIdentifier().getValue();
            Optional<EncryptTable> encryptTable = getEncryptRule().findEncryptTable(tableName);
            if (encryptTable.isPresent()) {
                Collection<SubstitutableColumnNameToken> sqlTokens = generateSQLTokens(projectionsSegment, tableName, selectStatementContext, encryptTable.get());
                substitutableColumnNameTokenList.addAll(sqlTokens);
            }
        }
        if (selectStatementContext.isContainsJoinQuery()) {
            substitutableColumnNameTokenList.addAll(processJoinTableSegments(selectStatementContext, simpleTableSegments));
        }
        
        return Collections.unmodifiableList(distinct(substitutableColumnNameTokenList));
    }
    
    private Collection<SubstitutableColumnNameToken> generateSQLTokens(final ProjectionsSegment segment, final String tableName, 
                                                                       final SelectStatementContext selectStatementContext, final EncryptTable encryptTable) {
        Collection<SubstitutableColumnNameToken> result = new LinkedList<>();
        for (ProjectionSegment each : segment.getProjections()) {
            if (each instanceof ColumnProjectionSegment) {
                if (encryptTable.getLogicColumns().contains(((ColumnProjectionSegment) each).getColumn().getIdentifier().getValue())) {
                    result.add(generateSQLToken((ColumnProjectionSegment) each, tableName));
                }
            }
            if (isToGeneratedSQLToken(each, selectStatementContext, tableName)) {
                ShorthandProjection shorthandProjection = getShorthandProjection((ShorthandProjectionSegment) each, selectStatementContext.getProjectionsContext());
                if (!shorthandProjection.getActualColumns().isEmpty()) {
                    result.add(generateSQLToken((ShorthandProjectionSegment) each, shorthandProjection, tableName, encryptTable, selectStatementContext.getDatabaseType()));
                }
            }
        }
        return result;
    }
    
    private List<SubstitutableColumnNameToken> distinct(final List<SubstitutableColumnNameToken> substitutableColumnNameTokenList) {
        Map<String, SubstitutableColumnNameToken> distinctMap = new HashMap<>();
        for (SubstitutableColumnNameToken each : substitutableColumnNameTokenList) {
            String key = each.getStartIndex() + "," + each.getStartIndex();
            if (!distinctMap.containsKey(key)) {
                distinctMap.put(key, each);
            }
        }
        return new LinkedList<>(distinctMap.values());
    }
    
    private Collection<SubstitutableColumnNameToken> processJoinTableSegments(final SelectStatementContext selectStatementContext, 
            final Collection<SimpleTableSegment> simpleTableSegments) {
        List<SubstitutableColumnNameToken> substitutableColumnNameTokenList = new ArrayList<>();
        Map<String, String> aliasTable = new HashMap<>(simpleTableSegments.size());
        for (SimpleTableSegment simpleTableSegment : simpleTableSegments) {
            String tableName = simpleTableSegment.getTableName().getIdentifier().getValue();
            String alias = tableName;
            if (simpleTableSegment.getAlias().isPresent()) {
                alias = simpleTableSegment.getAlias().get(); 
            }
            aliasTable.put(alias, tableName);
        }
        TableSegment tableSegment = selectStatementContext.getSqlStatement().getFrom();
        if (tableSegment instanceof JoinTableSegment) {
            JoinTableSegment joinTableSegment = (JoinTableSegment) tableSegment;
            TableSegment leftOne = joinTableSegment.getLeft();
            while (leftOne instanceof JoinTableSegment) {
                ExpressionSegment expressionSegment = joinTableSegment.getCondition();
                substitutableColumnNameTokenList.addAll(extractJoinTableSegmentCondition(expressionSegment, aliasTable));
                joinTableSegment = (JoinTableSegment) leftOne;
                leftOne = joinTableSegment.getLeft();
            }
            if (leftOne instanceof SimpleTableSegment) {
                ExpressionSegment expressionSegment = joinTableSegment.getCondition();
                substitutableColumnNameTokenList.addAll(extractJoinTableSegmentCondition(expressionSegment, aliasTable));
            }
        }
        return Collections.unmodifiableList(substitutableColumnNameTokenList);
    }
    
    private Collection<SubstitutableColumnNameToken> extractJoinTableSegmentCondition(final ExpressionSegment expressionSegment, 
             final Map<String, String> aliasTable) {
        List<SubstitutableColumnNameToken> substitutableColumnNameTokenList = new ArrayList<>();
        Collection<Optional<ColumnSegment>> columnSegments = ColumnExtractor.extractAll(expressionSegment);
        for (Optional<ColumnSegment> each : columnSegments) {
            ColumnSegment columnSegment = each.isPresent() ? each.get() : null;
            if (columnSegment == null) {
                continue;
            }
            String owner = columnSegment.getOwner().isPresent() ? columnSegment.getOwner().get().getIdentifier().getValue() : null;
            String logicColumn = columnSegment.getIdentifier().getValue();
            if (!aliasTable.containsKey(owner)) {
                continue;
            }
            Optional<String> assistedQueryColumn = getEncryptRule().findAssistedQueryColumn(aliasTable.get(owner), logicColumn);
            if (!assistedQueryColumn.isPresent()) {
                continue;
            }
            Collection<ColumnProjection> columnProjections = Arrays.asList(new ColumnProjection(owner, assistedQueryColumn.get(), null));
            substitutableColumnNameTokenList.add(new SubstitutableColumnNameToken(columnSegment.getStartIndex(), columnSegment.getStopIndex(), columnProjections));
        }
        return Collections.unmodifiableList(substitutableColumnNameTokenList);
    }
    
    private boolean isToGeneratedSQLToken(final ProjectionSegment projectionSegment, final SelectStatementContext selectStatementContext, final String tableName) {
        if (!(projectionSegment instanceof ShorthandProjectionSegment)) {
            return false;
        }
        Optional<OwnerSegment> ownerSegment = ((ShorthandProjectionSegment) projectionSegment).getOwner();
        return ownerSegment.map(segment -> selectStatementContext.getTablesContext().findTableNameFromSQL(segment.getIdentifier().getValue()).orElse("").equalsIgnoreCase(tableName)).orElse(true);
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ColumnProjectionSegment segment, final String tableName) {
        String encryptColumnName = getEncryptColumnName(tableName, segment.getColumn().getIdentifier().getValue());
        String alias = segment.getAlias().orElse(segment.getColumn().getIdentifier().getValue());
        Collection<ColumnProjection> projections = Collections.singletonList(new ColumnProjection(null, encryptColumnName, alias));
        return segment.getColumn().getOwner().isPresent() 
                ? new SubstitutableColumnNameToken(segment.getColumn().getOwner().get().getStopIndex() + 2, segment.getStopIndex(), projections) 
                : new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections);
    }
    
    private SubstitutableColumnNameToken generateSQLToken(final ShorthandProjectionSegment segment,
                                                          final ShorthandProjection shorthandProjection, final String tableName, final EncryptTable encryptTable, final DatabaseType databaseType) {
        List<ColumnProjection> projections = new LinkedList<>();
        for (ColumnProjection each : shorthandProjection.getActualColumns().values()) {
            if (encryptTable.getLogicColumns().contains(each.getName())) {
                projections.add(new ColumnProjection(null == each.getOwner() ? null : each.getOwner(), getEncryptColumnName(tableName, each.getName()), each.getName()));
            } else {
                projections.add(new ColumnProjection(null == each.getOwner() ? null : each.getOwner(), each.getName(), null));
            }
        }
        previousSQLTokens.removeIf(each -> each.getStartIndex() == segment.getStartIndex());
        return new SubstitutableColumnNameToken(segment.getStartIndex(), segment.getStopIndex(), projections, databaseType.getQuoteCharacter());
    }
    
    private String getEncryptColumnName(final String tableName, final String logicEncryptColumnName) {
        Optional<String> plainColumn = getEncryptRule().findPlainColumn(tableName, logicEncryptColumnName);
        return plainColumn.isPresent() && !queryWithCipherColumn ? plainColumn.get() : getEncryptRule().getCipherColumn(tableName, logicEncryptColumnName);
    }
    
    private ShorthandProjection getShorthandProjection(final ShorthandProjectionSegment segment, final ProjectionsContext projectionsContext) {
        Optional<String> owner = segment.getOwner().isPresent() ? Optional.of(segment.getOwner().get().getIdentifier().getValue()) : Optional.empty();
        for (Projection each : projectionsContext.getProjections()) {
            if (each instanceof ShorthandProjection) {
                if (!owner.isPresent() && !((ShorthandProjection) each).getOwner().isPresent()) {
                    return (ShorthandProjection) each;
                }
                if (owner.isPresent() && owner.get().equals(((ShorthandProjection) each).getOwner().orElse(null))) {
                    return (ShorthandProjection) each;
                }
            }
        }
        throw new IllegalStateException(String.format("Can not find shorthand projection segment, owner is: `%s`", owner.orElse(null)));
    }
    
    @Override
    public void setPreviousSQLTokens(final List<SQLToken> previousSQLTokens) {
        this.previousSQLTokens = previousSQLTokens;
    }
}
