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

package org.apache.shardingsphere.singletable.route;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.singletable.constant.SingleTableOrder;
import org.apache.shardingsphere.singletable.route.engine.SingleTableRouteEngineFactory;
import org.apache.shardingsphere.singletable.route.validator.SingleTableMetadataValidator;
import org.apache.shardingsphere.singletable.route.validator.SingleTableMetadataValidatorFactory;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Single table SQL router.
 */
public final class SingleTableSQLRouter implements SQLRouter<SingleTableRule> {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public RouteContext createRouteContext(final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final SingleTableRule rule, final ConfigurationProperties props) {
        if (1 == metaData.getResource().getDataSources().size()) {
            return createSingleDataSourceRouteContext(rule, metaData);
        }
        RouteContext result = new RouteContext();
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        Optional<SingleTableMetadataValidator> validator = SingleTableMetadataValidatorFactory.newInstance(sqlStatementContext.getSqlStatement());
        validator.ifPresent(optional -> optional.validate(rule, sqlStatementContext, metaData));
        Collection<QualifiedTable> singleTableNames = getSingleTableNames(sqlStatementContext, metaData, rule, result);
        if (!singleTableNames.isEmpty()) {
            validateSameDataSource(sqlStatementContext, rule, props, singleTableNames, result);
        }
        SingleTableRouteEngineFactory.newInstance(singleTableNames, sqlStatementContext.getSqlStatement()).ifPresent(optional -> optional.route(result, rule));
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final LogicSQL logicSQL, final ShardingSphereMetaData metaData,
                                     final SingleTableRule rule, final ConfigurationProperties props) {
        SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
        Collection<QualifiedTable> singleTableNames = getSingleTableNames(sqlStatementContext, metaData, rule, routeContext);
        if (singleTableNames.isEmpty()) {
            return;
        }
        validateSameDataSource(sqlStatementContext, rule, props, singleTableNames, routeContext);
        SingleTableRouteEngineFactory.newInstance(singleTableNames, sqlStatementContext.getSqlStatement()).ifPresent(optional -> optional.route(routeContext, rule));
    }
    
    private RouteContext createSingleDataSourceRouteContext(final SingleTableRule rule, final ShardingSphereMetaData metaData) {
        String logicDataSource = rule.getDataSourceNames().iterator().next();
        String actualDataSource = metaData.getResource().getDataSources().keySet().iterator().next();
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(logicDataSource, actualDataSource), Collections.emptyList()));
        return result;
    }
    
    private static Collection<QualifiedTable> getSingleTableNames(final SQLStatementContext<?> sqlStatementContext, final ShardingSphereMetaData metaData,
                                                                  final SingleTableRule rule, final RouteContext routeContext) {
        DatabaseType databaseType = sqlStatementContext.getDatabaseType();
        Collection<QualifiedTable> result = getQualifiedTables(metaData, sqlStatementContext.getTablesContext().getTables(), databaseType);
        if (result.isEmpty() && sqlStatementContext instanceof IndexAvailable) {
            result = IndexMetaDataUtil.getTableNamesFromMetaData(metaData, ((IndexAvailable) sqlStatementContext).getIndexes(), databaseType);
        }
        return routeContext.getRouteUnits().isEmpty() && sqlStatementContext.getSqlStatement() instanceof CreateTableStatement ? result : rule.getSingleTableNames(result);
    }
    
    private static Collection<QualifiedTable> getQualifiedTables(final ShardingSphereMetaData metaData, final Collection<SimpleTableSegment> tableSegments, final DatabaseType databaseType) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = databaseType.getDefaultSchema(metaData.getDatabaseName());
        for (SimpleTableSegment each : tableSegments) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            result.add(new QualifiedTable(actualSchemaName, each.getTableName().getIdentifier().getValue()));
        }
        return result;
    }
    
    private static void validateSameDataSource(final SQLStatementContext<?> sqlStatementContext, final SingleTableRule rule,
                                               final ConfigurationProperties props, final Collection<QualifiedTable> singleTableNames, final RouteContext routeContext) {
        boolean sqlFederationEnabled = props.getValue(ConfigurationPropertyKey.SQL_FEDERATION_ENABLED);
        boolean allTablesInSameDataSource = sqlFederationEnabled
                ? sqlStatementContext instanceof SelectStatementContext || rule.isSingleTablesInSameDataSource(singleTableNames)
                : rule.isAllTablesInSameDataSource(routeContext, singleTableNames);
        Preconditions.checkState(allTablesInSameDataSource, "All tables must be in the same datasource.");
    }
    
    @Override
    public int getOrder() {
        return SingleTableOrder.ORDER;
    }
    
    @Override
    public Class<SingleTableRule> getTypeClass() {
        return SingleTableRule.class;
    }
}
