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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.driver.jdbc.adapter.WrapperAdapter;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.syntax.ColumnIndexOutOfRangeException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ShardingSphere result set meta data.
 */
@RequiredArgsConstructor
public final class ShardingSphereResultSetMetaData extends WrapperAdapter implements ResultSetMetaData {
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final ShardingSphereDatabase database;
    
    private final SQLStatementContext sqlStatementContext;
    
    private volatile ClientVisibleColumnLayout clientVisibleColumnLayout;
    
    @Override
    public int getColumnCount() throws SQLException {
        ClientVisibleColumnLayout columnLayout = getClientVisibleColumnLayout();
        if (columnLayout.useExpandedProjections) {
            return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().size();
        }
        return columnLayout.passthrough ? resultSetMetaData.getColumnCount() : columnLayout.visibleColumnIndexes.length;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return resultSetMetaData.isAutoIncrement(column);
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        return resultSetMetaData.isCaseSensitive(column);
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        return resultSetMetaData.isSearchable(column);
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        return resultSetMetaData.isCurrency(column);
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        return resultSetMetaData.isNullable(column);
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return resultSetMetaData.isSigned(column);
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        return resultSetMetaData.getColumnDisplaySize(column);
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        ClientVisibleColumnLayout columnLayout = getClientVisibleColumnLayout();
        if (columnLayout.useExpandedProjections) {
            checkColumnIndex(column);
            return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().get(column - 1).getColumnLabel();
        }
        String columnLabel = resultSetMetaData.getColumnLabel(getReturnedColumnIndex(columnLayout, column));
        return columnLayout.aggregationDistinctColumnLabels.getOrDefault(columnLabel, columnLabel);
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        ClientVisibleColumnLayout columnLayout = getClientVisibleColumnLayout();
        if (columnLayout.useExpandedProjections) {
            checkColumnIndex(column);
            return ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().get(column - 1).getColumnName();
        }
        int returnedColumnIndex = getReturnedColumnIndex(columnLayout, column);
        if (!columnLayout.aggregationDistinctColumnLabels.isEmpty()) {
            String columnLabel = resultSetMetaData.getColumnLabel(returnedColumnIndex);
            if (columnLayout.aggregationDistinctColumnLabels.containsKey(columnLabel)) {
                return columnLayout.aggregationDistinctColumnLabels.get(columnLabel);
            }
        }
        return resultSetMetaData.getColumnName(returnedColumnIndex);
    }
    
    private int getReturnedColumnIndex(final ClientVisibleColumnLayout columnLayout, final int column) throws SQLException {
        return columnLayout.passthrough ? column : columnLayout.getVisibleColumnIndex(column);
    }
    
    private ClientVisibleColumnLayout getClientVisibleColumnLayout() throws SQLException {
        ClientVisibleColumnLayout result = clientVisibleColumnLayout;
        if (null == result) {
            result = ClientVisibleColumnLayout.create(sqlStatementContext, resultSetMetaData);
            clientVisibleColumnLayout = result;
        }
        return result;
    }
    
    private void checkColumnIndex(final int column) throws SQLException {
        List<Projection> actualProjections = ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections();
        if (column > actualProjections.size()) {
            throw new ColumnIndexOutOfRangeException(column).toSQLException();
        }
    }
    
    @Override
    public String getSchemaName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        return resultSetMetaData.getPrecision(column);
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        return resultSetMetaData.getScale(column);
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        String actualTableName = resultSetMetaData.getTableName(column);
        return null == database ? actualTableName : decorateTableName(database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class), actualTableName);
    }
    
    private String decorateTableName(final Collection<DataNodeRuleAttribute> ruleAttributes, final String actualTableName) {
        for (DataNodeRuleAttribute each : ruleAttributes) {
            if (each.findLogicTableByActualTable(actualTableName).isPresent()) {
                return each.findLogicTableByActualTable(actualTableName).get();
            }
        }
        return actualTableName;
    }
    
    @Override
    public String getCatalogName(final int column) {
        return DefaultDatabase.LOGIC_NAME;
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        return resultSetMetaData.getColumnType(column);
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        return resultSetMetaData.getColumnTypeName(column);
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return resultSetMetaData.isReadOnly(column);
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return resultSetMetaData.isWritable(column);
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return resultSetMetaData.isDefinitelyWritable(column);
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        return resultSetMetaData.getColumnClassName(column);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ClientVisibleColumnLayout {
        
        private final boolean useExpandedProjections;
        
        private final boolean passthrough;
        
        private final int[] visibleColumnIndexes;
        
        private final Map<String, String> aggregationDistinctColumnLabels;
        
        private static ClientVisibleColumnLayout create(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
            if (ShardingSphereResultSetUtils.useExpandedProjections(sqlStatementContext, resultSetMetaData)) {
                return new ClientVisibleColumnLayout(true, false, new int[0], Collections.emptyMap());
            }
            if (!ShardingSphereResultSetUtils.mayAppendDerivedColumns(sqlStatementContext)) {
                return new ClientVisibleColumnLayout(false, true, new int[0], Collections.emptyMap());
            }
            int columnCount = resultSetMetaData.getColumnCount();
            Set<Integer> appendedDerivedColumnIndexes = ShardingSphereResultSetUtils.getAppendedDerivedColumnIndexes(sqlStatementContext, resultSetMetaData);
            int[] result = new int[columnCount];
            int visibleColumnCount = 0;
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                if (!appendedDerivedColumnIndexes.contains(columnIndex)) {
                    result[visibleColumnCount++] = columnIndex;
                }
            }
            return new ClientVisibleColumnLayout(false, false, Arrays.copyOf(result, visibleColumnCount), ShardingSphereResultSetUtils.createAggregationDistinctColumnLabels(sqlStatementContext));
        }
        
        private int getVisibleColumnIndex(final int column) throws SQLException {
            if (column < 1 || column > visibleColumnIndexes.length) {
                throw new ColumnIndexOutOfRangeException(column).toSQLException();
            }
            return visibleColumnIndexes[column - 1];
        }
    }
}
