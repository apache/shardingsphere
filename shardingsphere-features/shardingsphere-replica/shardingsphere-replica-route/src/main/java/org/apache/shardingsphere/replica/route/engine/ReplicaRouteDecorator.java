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

package org.apache.shardingsphere.replica.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.decorator.RouteDecorator;
import org.apache.shardingsphere.replica.constant.ReplicaOrder;
import org.apache.shardingsphere.replica.rule.ReplicaTableRule;
import org.apache.shardingsphere.replica.rule.ReplicaRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.CacheIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ChecksumTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.FlushStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.InstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.KillStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.LoadIndexInfoStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.RepairTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ResetStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UninstallPluginStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DenyUserStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Route decorator for replica.
 */
public final class ReplicaRouteDecorator implements RouteDecorator<ReplicaRule> {
    
    @Override
    public RouteContext decorate(final RouteContext routeContext, final ShardingSphereMetaData metaData, final ReplicaRule replicaRule, final ConfigurationProperties props) {
        RouteStageContext preRouteStageContext = routeContext.lastRouteStageContext();
        Map<String, ReplicaGroup> replicaGroups = new HashMap<>();
        if (preRouteStageContext.getRouteResult().getRouteUnits().isEmpty()) {
            ReplicaTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
            ReplicaGroup replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                    replicaRoutingRule.getDataSourceName());
            replicaGroups.put(ReplicaGroup.BLANK_REPLICA_GROUP_KEY, replicaGroup);
            addRouteStageContext(routeContext, preRouteStageContext, replicaGroups);
            return routeContext;
        }
        for (RouteUnit each: preRouteStageContext.getRouteResult().getRouteUnits()) {
            Collection<RouteMapper> routeMappers = each.getTableMappers();
            if (null == routeMappers || routeMappers.isEmpty()) {
                ReplicaTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
                ReplicaGroup replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
                replicaGroups.put(ReplicaGroup.BLANK_REPLICA_GROUP_KEY, replicaGroup);
            } else {
                routeReplicaGroups(routeMappers, replicaRule, replicaGroups);
            }
        }
        addRouteStageContext(routeContext, preRouteStageContext, replicaGroups);
        return routeContext;
    }

    private void addRouteStageContext(final RouteContext routeContext, final RouteStageContext preRouteStageContext, final Map<String, ReplicaGroup> routeMapping) {
        final SQLStatementContext sqlStatementContext = preRouteStageContext.getSqlStatementContext();
        routeContext.addRouteStageContext(getOrder(), new ReplicaRouteStageContext(preRouteStageContext.getCurrentSchemaName(), sqlStatementContext, preRouteStageContext.getParameters(),
                preRouteStageContext.getRouteResult(), routeMapping, isReadOnly(sqlStatementContext)));
    }

    private void routeReplicaGroups(final Collection<RouteMapper> routeMappers, final ReplicaRule replicaRule, final Map<String, ReplicaGroup> replicaGroups) {
        for (RouteMapper each: routeMappers) {
            String actualTableName = each.getActualName();
            Optional<ReplicaTableRule> replicaRoutingRuleOptional = replicaRule.findRoutingByTable(actualTableName);
            ReplicaGroup replicaGroup;
            if (replicaRoutingRuleOptional.isPresent()) {
                ReplicaTableRule replicaRoutingRule = replicaRoutingRuleOptional.get();
                replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
                replicaGroups.put(actualTableName, replicaGroup);
            } else {
                ReplicaTableRule replicaRoutingRule = replicaRule.getReplicaTableRules().iterator().next();
                replicaGroup = new ReplicaGroup(replicaRoutingRule.getPhysicsTable(), replicaRoutingRule.getReplicaGroupId(), replicaRoutingRule.getReplicaPeers(),
                        replicaRoutingRule.getDataSourceName());
            }
            replicaGroups.put(actualTableName, replicaGroup);
        }
    }

    /**
     * Get SQL OPType.
     *
     * @return OPType: true-read, false-write
     */
    private boolean isReadOnly(final SQLStatementContext sqlStatementContext) {
        return isReadOnly(sqlStatementContext.getSqlStatement());
    }

    /**
     * Get SQL OPType.
     *
     * @param sqlStatement SQLStatement
     * @return OPType: true-read, false-write
     */
    private boolean isReadOnly(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DMLStatement) {
            return isReadOnly((DMLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return isReadOnly((DDLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DCLStatement) {
            return isReadOnly((DCLStatement) sqlStatement);
        }
        if (sqlStatement instanceof DALStatement) {
            return isReadOnly((DALStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }

    /**
     * Get SQL OPType.
     *
     * @param sqlStatement SQLStatement
     * @return OPType: true-read, false-write
     */
    private boolean isReadOnly(final DMLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return true;
        } else if (sqlStatement instanceof UpdateStatement
                | sqlStatement instanceof DeleteStatement
                | sqlStatement instanceof InsertStatement) {
            return false;
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL Type `%s`", sqlStatement.getClass().getSimpleName()));
    }

    /**
     * Get SQL OPType.
     *
     * @param sqlStatement SQLStatement
     * @return OPType: true-read, false-write
     */
    private boolean isReadOnly(final DDLStatement sqlStatement) {
        if (sqlStatement instanceof CreateTableStatement
                | sqlStatement instanceof AlterTableStatement
                | sqlStatement instanceof DropTableStatement
                | sqlStatement instanceof CreateIndexStatement
                | sqlStatement instanceof AlterIndexStatement
                | sqlStatement instanceof DropIndexStatement
                | sqlStatement instanceof TruncateStatement
                | sqlStatement instanceof AlterTableStatement) {
            return false;
        }
        return false;
    }

    /**
     * Get SQL OPType.
     *
     * @param sqlStatement SQLStatement
     * @return OPType: true-read, false-write
     */
    private boolean isReadOnly(final DCLStatement sqlStatement) {
        if (sqlStatement instanceof GrantStatement
                | sqlStatement instanceof RevokeStatement
                | sqlStatement instanceof DenyUserStatement) {
            return false;
        }
        return false;
    }

    /**
     * Get SQL OPType.
     *
     * @param sqlStatement SQLStatement
     * @return OPType: true-read, false-write
     */
    private boolean isReadOnly(final DALStatement sqlStatement) {
        if (sqlStatement instanceof SetStatement
                | sqlStatement instanceof UseStatement
                | sqlStatement instanceof UninstallPluginStatement
                | sqlStatement instanceof ResetStatement
                | sqlStatement instanceof RepairTableStatement
                | sqlStatement instanceof OptimizeTableStatement
                | sqlStatement instanceof LoadIndexInfoStatement
                | sqlStatement instanceof KillStatement
                | sqlStatement instanceof InstallPluginStatement
                | sqlStatement instanceof FlushStatement
                | sqlStatement instanceof ChecksumTableStatement
                | sqlStatement instanceof CacheIndexStatement) {
            return false;
        }
        return true;
    }
    
    @Override
    public int getOrder() {
        return ReplicaOrder.ORDER;
    }
    
    @Override
    public Class<ReplicaRule> getTypeClass() {
        return ReplicaRule.class;
    }
}
