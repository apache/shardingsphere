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

package org.apache.shardingsphere.shadow.route.future.engine.dml;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.shadow.route.future.engine.AbstractShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowTableDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shadow insert statement routing engine.
 */
public final class ShadowInsertStatementRoutingEngine extends AbstractShadowRouteEngine {
    
    @Override
    public void route(final RouteContext routeContext, final LogicSQL logicSQL, final ShardingSphereMetaData metaData, final ShadowRule shadowRule, final ConfigurationProperties props) {
        if (isShadow(logicSQL, shadowRule)) {
            shadowDMLStatementRouteDecorate(routeContext, shadowRule);
        }
    }
    
    private boolean isShadow(final LogicSQL logicSQL, final ShadowRule shadowRule) {
        InsertStatementContext insertStatementContext = (InsertStatementContext) logicSQL.getSqlStatementContext();
        Collection<String> relatedShadowTables = getRelatedShadowTables(insertStatementContext, shadowRule);
        initShadowTableDeterminer(relatedShadowTables, shadowRule);
        for (String each : relatedShadowTables) {
            getShadowTableDeterminer(each).ifPresent(tableDeterminer -> tableDeterminer.isShadow(insertStatementContext, shadowRule, each));
        }
        return false;
    }
    
    private Collection<String> getRelatedShadowTables(final InsertStatementContext insertStatementContext, final ShadowRule shadowRule) {
        return shadowRule.getRelatedShadowTables(insertStatementContext.getAllTables().stream().map(each -> each.getTableName().getIdentifier().getValue())
                .collect(Collectors.toCollection(LinkedList::new)));
    }
    
    private void initShadowTableDeterminer(final Collection<String> relatedShadowTables, final ShadowRule shadowRule) {
        for (String each : relatedShadowTables) {
            Optional<ShadowTableDeterminer> shadowTableDeterminer = ShadowDeterminerFactory.getShadowTableDeterminer(each, shadowRule);
            shadowTableDeterminer.ifPresent(tableDeterminer -> getShadowTableDeterminers().put(each, tableDeterminer));
        }
    }
}
