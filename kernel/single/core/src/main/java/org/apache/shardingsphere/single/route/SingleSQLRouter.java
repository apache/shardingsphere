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

package org.apache.shardingsphere.single.route;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.validator.ShardingSphereMetaDataValidateUtils;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.route.engine.SingleRouteEngineFactory;
import org.apache.shardingsphere.single.route.validator.SingleMetaDataValidator;
import org.apache.shardingsphere.single.route.validator.SingleMetaDataValidatorFactory;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Single SQL router.
 */
public final class SingleSQLRouter implements SQLRouter<SingleRule> {
    
    @Override
    public RouteContext createRouteContext(final QueryContext queryContext, final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final SingleRule rule,
                                           final ConfigurationProperties props, final ConnectionContext connectionContext) {
        if (1 == database.getResourceMetaData().getDataSources().size()) {
            return createSingleDataSourceRouteContext(rule, database);
        }
        RouteContext result = new RouteContext();
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        Optional<SingleMetaDataValidator> validator = SingleMetaDataValidatorFactory.newInstance(sqlStatement);
        validator.ifPresent(optional -> optional.validate(rule, sqlStatementContext, database));
        Collection<QualifiedTable> singleTableNames = getSingleTableNames(sqlStatementContext, database, rule, result);
        if (!singleTableNames.isEmpty()) {
            if (sqlStatement instanceof InsertStatement || sqlStatement instanceof DeleteStatement || sqlStatement instanceof UpdateStatement || sqlStatement instanceof SelectStatement) {
                ShardingSphereMetaDataValidateUtils.validateTableExist(sqlStatementContext, database);
            }
            validateSameDataSource(rule, singleTableNames, result);
        }
        SingleRouteEngineFactory.newInstance(singleTableNames, sqlStatement).ifPresent(optional -> optional.route(result, rule));
        return result;
    }
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final SingleRule rule, final ConfigurationProperties props, final ConnectionContext connectionContext) {
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        Collection<QualifiedTable> singleTableNames = getSingleTableNames(sqlStatementContext, database, rule, routeContext);
        if (singleTableNames.isEmpty()) {
            return;
        }
        validateSameDataSource(rule, singleTableNames, routeContext);
        SingleRouteEngineFactory.newInstance(singleTableNames, sqlStatementContext.getSqlStatement()).ifPresent(optional -> optional.route(routeContext, rule));
    }
    
    private RouteContext createSingleDataSourceRouteContext(final SingleRule rule, final ShardingSphereDatabase database) {
        String logicDataSource = rule.getDataSourceNames().iterator().next();
        String actualDataSource = database.getResourceMetaData().getDataSources().keySet().iterator().next();
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(logicDataSource, actualDataSource), Collections.emptyList()));
        return result;
    }
    
    private Collection<QualifiedTable> getSingleTableNames(final SQLStatementContext sqlStatementContext,
                                                           final ShardingSphereDatabase database, final SingleRule rule, final RouteContext routeContext) {
        DatabaseType databaseType = sqlStatementContext.getDatabaseType();
        Collection<QualifiedTable> result = getQualifiedTables(database, databaseType, sqlStatementContext.getTablesContext().getSimpleTableSegments());
        if (result.isEmpty() && sqlStatementContext instanceof IndexAvailable) {
            result = IndexMetaDataUtils.getTableNames(database, databaseType, ((IndexAvailable) sqlStatementContext).getIndexes());
        }
        return routeContext.getRouteUnits().isEmpty() && sqlStatementContext.getSqlStatement() instanceof CreateTableStatement ? result : rule.getSingleTableNames(result);
    }
    
    private Collection<QualifiedTable> getQualifiedTables(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<SimpleTableSegment> tableSegments) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(databaseType, database.getName());
        for (SimpleTableSegment each : tableSegments) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            result.add(new QualifiedTable(actualSchemaName, each.getTableName().getIdentifier().getValue()));
        }
        return result;
    }
    
    private void validateSameDataSource(final SingleRule rule, final Collection<QualifiedTable> singleTableNames, final RouteContext routeContext) {
        Preconditions.checkState(rule.isAllTablesInSameDataSource(routeContext, singleTableNames), "All tables must be in the same datasource.");
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRule> getTypeClass() {
        return SingleRule.class;
    }
}
