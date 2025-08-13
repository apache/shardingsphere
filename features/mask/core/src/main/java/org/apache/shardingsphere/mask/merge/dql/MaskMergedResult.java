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

package org.apache.shardingsphere.mask.merge.dql;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Merged result for mask.
 */
public final class MaskMergedResult extends DecoratorMergedResult {
    
    private final ShardingSphereDatabase database;
    
    private final ShardingSphereMetaData metaData;
    
    private final SelectStatementContext selectStatementContext;
    
    public MaskMergedResult(final ShardingSphereDatabase database, final ShardingSphereMetaData metaData, final SelectStatementContext selectStatementContext, final MergedResult mergedResult) {
        super(mergedResult);
        this.database = database;
        this.metaData = metaData;
        this.selectStatementContext = selectStatementContext;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Optional<ColumnSegmentBoundInfo> columnSegmentBoundInfo = selectStatementContext.findColumnBoundInfo(columnIndex);
        if (!columnSegmentBoundInfo.isPresent()) {
            return getMergedResult().getValue(columnIndex, type);
        }
        String originalTableName = columnSegmentBoundInfo.get().getOriginalTable().getValue();
        String originalColumnName = columnSegmentBoundInfo.get().getOriginalColumn().getValue();
        ShardingSphereDatabase database = metaData.containsDatabase(columnSegmentBoundInfo.get().getOriginalDatabase().getValue())
                ? metaData.getDatabase(columnSegmentBoundInfo.get().getOriginalDatabase().getValue())
                : this.database;
        Optional<MaskRule> rule = database.getRuleMetaData().findSingleRule(MaskRule.class);
        if (!rule.isPresent() || !rule.get().findMaskTable(originalTableName).map(optional -> optional.findAlgorithm(originalColumnName).isPresent()).orElse(false)) {
            return getMergedResult().getValue(columnIndex, type);
        }
        Optional<MaskAlgorithm> maskAlgorithm = rule.get().findMaskTable(originalTableName).flatMap(optional -> optional.findAlgorithm(originalColumnName));
        if (!maskAlgorithm.isPresent()) {
            return getMergedResult().getValue(columnIndex, type);
        }
        Object originalValue = getMergedResult().getValue(columnIndex, Object.class);
        return null == originalValue ? null : maskAlgorithm.get().mask(originalValue);
    }
}
