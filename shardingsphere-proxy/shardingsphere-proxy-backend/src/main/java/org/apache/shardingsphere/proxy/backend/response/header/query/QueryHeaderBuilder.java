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

package org.apache.shardingsphere.proxy.backend.response.header.query;

import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.infra.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeAwareSPI;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;

import java.sql.SQLException;

/**
 * Query header builder.
 */
public abstract class QueryHeaderBuilder implements DatabaseTypeAwareSPI {
    
    /**
     * Build query header builder.
     *
     * @param queryResultMetaData query result meta data
     * @param metaData ShardingSphere meta data
     * @param columnIndex column index 
     * @param dataNodeContainedRule data node contained rule
     * @return query header
     * @throws SQLException SQL exception
     */
    public final QueryHeader build(final QueryResultMetaData queryResultMetaData, final ShardingSphereMetaData metaData,
                                   final int columnIndex, final LazyInitializer<DataNodeContainedRule> dataNodeContainedRule) throws SQLException {
        return doBuild(queryResultMetaData, metaData, queryResultMetaData.getColumnName(columnIndex), queryResultMetaData.getColumnLabel(columnIndex), columnIndex, dataNodeContainedRule);
    }
    
    /**
     * Build query header builder.
     *
     * @param projectionsContext projections context
     * @param queryResultMetaData query result meta data
     * @param metaData ShardingSphere meta data
     * @param columnIndex column index
     * @param dataNodeContainedRule data node contained rule
     * @return query header
     * @throws SQLException SQL exception
     */
    public final QueryHeader build(final ProjectionsContext projectionsContext, final QueryResultMetaData queryResultMetaData,
                                    final ShardingSphereMetaData metaData, final int columnIndex, final LazyInitializer<DataNodeContainedRule> dataNodeContainedRule) throws SQLException {
        return doBuild(queryResultMetaData, metaData, getColumnName(projectionsContext, queryResultMetaData, columnIndex), 
                getColumnLabel(projectionsContext, queryResultMetaData, columnIndex), columnIndex, dataNodeContainedRule);
    }
    
    private String getColumnLabel(final ProjectionsContext projectionsContext, final QueryResultMetaData queryResultMetaData, final int columnIndex) throws SQLException {
        Projection projection = projectionsContext.getExpandProjections().get(columnIndex - 1);
        return DerivedColumn.isDerivedColumnName(projection.getColumnLabel()) ? projection.getExpression() : queryResultMetaData.getColumnLabel(columnIndex);
    }
    
    private String getColumnName(final ProjectionsContext projectionsContext, final QueryResultMetaData queryResultMetaData, final int columnIndex) throws SQLException {
        Projection projection = projectionsContext.getExpandProjections().get(columnIndex - 1);
        return projection instanceof ColumnProjection ? ((ColumnProjection) projection).getName() : queryResultMetaData.getColumnName(columnIndex);
    }
    
    protected abstract QueryHeader doBuild(QueryResultMetaData queryResultMetaData, ShardingSphereMetaData metaData, String columnName, String columnLabel, int columnIndex,
                                           LazyInitializer<DataNodeContainedRule> dataNodeContainedRule) throws SQLException;
}
