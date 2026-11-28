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

package org.apache.shardingsphere.shadow.route;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.lifecycle.DecorateSQLRouter;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.route.determiner.ShadowInsertValueRouteDecider;
import org.apache.shardingsphere.shadow.route.retriever.ShadowDataSourceMappingsRetrieverFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Shadow SQL router.
 */
@HighFrequencyInvocation
public final class ShadowSQLRouter implements DecorateSQLRouter<ShadowRule> {
    
    @Override
    public void decorateRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShardingSphereDatabase database,
                                     final ShadowRule rule, final Collection<String> tableNames, final ConfigurationProperties props) {
        Map<String, String> shadowDataSourceMappings = ShadowDataSourceMappingsRetrieverFactory.newInstance(queryContext).retrieve(rule);
        if (shadowDataSourceMappings.isEmpty() && decorateMultiValuesInsertRouteContext(routeContext, queryContext, rule)) {
            return;
        }
        Collection<RouteUnit> toBeRemovedRouteUnit = new LinkedList<>();
        Collection<RouteUnit> toBeAddedRouteUnit = new LinkedList<>();
        for (RouteUnit each : routeContext.getRouteUnits()) {
            String logicName = each.getDataSourceMapper().getLogicName();
            String actualName = each.getDataSourceMapper().getActualName();
            Optional<String> productionDataSourceName = rule.findProductionDataSourceName(actualName);
            if (productionDataSourceName.isPresent()) {
                String shadowDataSourceName = shadowDataSourceMappings.get(productionDataSourceName.get());
                toBeRemovedRouteUnit.add(each);
                String dataSourceName = null == shadowDataSourceName ? productionDataSourceName.get() : shadowDataSourceName;
                toBeAddedRouteUnit.add(new RouteUnit(new RouteMapper(logicName, dataSourceName), each.getTableMappers()));
            }
        }
        routeContext.getRouteUnits().removeAll(toBeRemovedRouteUnit);
        routeContext.getRouteUnits().addAll(toBeAddedRouteUnit);
    }
    
    private boolean decorateMultiValuesInsertRouteContext(final RouteContext routeContext, final QueryContext queryContext, final ShadowRule rule) {
        if (1 != routeContext.getRouteUnits().size() || !routeContext.getOriginalDataNodes().isEmpty() || routeContext.containsTableSharding()) {
            return false;
        }
        if (!(queryContext.getSqlStatementContext() instanceof InsertStatementContext)) {
            return false;
        }
        InsertStatementContext insertStatementContext = (InsertStatementContext) queryContext.getSqlStatementContext();
        if (queryContext.getHintValueContext().isSkipSQLRewrite() || insertStatementContext.getSqlStatement().getReturning().isPresent()) {
            return false;
        }
        Collection<String> tableNames = insertStatementContext.getTablesContext().getTableNames();
        if (tableNames.isEmpty()) {
            return false;
        }
        String tableName = tableNames.iterator().next();
        if (rule.filterShadowTables(Collections.singleton(tableName)).isEmpty()) {
            return false;
        }
        Collection<Boolean> shadowRows = new ShadowInsertValueRouteDecider(rule, insertStatementContext).decide();
        if (!shadowRows.contains(Boolean.TRUE)) {
            return false;
        }
        RouteUnit routeUnit = routeContext.getRouteUnits().iterator().next();
        Optional<String> productionDataSourceName = rule.findProductionDataSourceName(routeUnit.getDataSourceMapper().getActualName());
        if (!productionDataSourceName.isPresent()) {
            return false;
        }
        String shadowDataSourceName = rule.getShadowDataSourceMappings(tableName).get(productionDataSourceName.get());
        if (null == shadowDataSourceName || shadowDataSourceName.equalsIgnoreCase(productionDataSourceName.get())) {
            return false;
        }
        if (isMixedShadowRows(shadowRows)) {
            addSplitInsertRouteUnits(routeContext, routeUnit, shadowRows, productionDataSourceName.get(), shadowDataSourceName);
        } else {
            routeContext.getRouteUnits().clear();
            routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(routeUnit.getDataSourceMapper().getLogicName(), shadowDataSourceName), routeUnit.getTableMappers()));
        }
        return true;
    }
    
    private boolean isMixedShadowRows(final Collection<Boolean> shadowRows) {
        return shadowRows.contains(Boolean.TRUE) && shadowRows.contains(Boolean.FALSE);
    }
    
    private void addSplitInsertRouteUnits(final RouteContext routeContext, final RouteUnit routeUnit,
                                          final Collection<Boolean> shadowRows, final String productionDataSourceName, final String shadowDataSourceName) {
        RouteUnit productionRouteUnit =
                new RouteUnit(new RouteMapper(routeUnit.getDataSourceMapper().getLogicName(), productionDataSourceName), routeUnit.getTableMappers());
        RouteUnit shadowRouteUnit = new RouteUnit(new RouteMapper(routeUnit.getDataSourceMapper().getLogicName(), shadowDataSourceName), routeUnit.getTableMappers());
        Collection<Integer> productionInsertValueIndexes = new LinkedList<>();
        Collection<Integer> shadowInsertValueIndexes = new LinkedList<>();
        int index = 0;
        for (boolean each : shadowRows) {
            (each ? shadowInsertValueIndexes : productionInsertValueIndexes).add(index);
            index++;
        }
        routeContext.getRouteUnits().clear();
        routeContext.getRouteUnits().add(productionRouteUnit);
        routeContext.getRouteUnits().add(shadowRouteUnit);
        routeContext.getRouteUnitInsertValueIndexes().put(productionRouteUnit, productionInsertValueIndexes);
        routeContext.getRouteUnitInsertValueIndexes().put(shadowRouteUnit, shadowInsertValueIndexes);
    }
    
    @Override
    public Type getType() {
        return Type.DATA_SOURCE;
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
    
    @Override
    public Class<ShadowRule> getTypeClass() {
        return ShadowRule.class;
    }
}
