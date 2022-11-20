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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableGlobalRuleRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableGlobalRuleRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodeModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowComputeNodesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.QueryableScalingRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ApplyDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DiscardDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelComputeNodeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.PrepareDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshDatabaseMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelComputeNodeStatement;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.HintRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.query.QueryableScalingRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.query.QueryableScalingRALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.update.UpdatableScalingRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ConvertYamlConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ExportDatabaseConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowComputeNodeInfoHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowComputeNodeModeHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowComputeNodesHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowDistVariableHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowDistVariablesHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowStatusFromReadwriteSplittingRulesHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.AlterReadwriteSplittingStorageUnitStatusStatementHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.ApplyDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.DiscardDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.ImportDatabaseConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.LabelComputeNodeHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.PrepareDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.RefreshDatabaseMetadataHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.RefreshTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.SetDistVariableHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.SetInstanceStatusHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.UnlabelComputeNodeHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.AlterReadwriteSplittingStorageUnitStatusStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    private static final Map<Class<? extends RALStatement>, Class<? extends RALBackendHandler<?>>> HANDLERS = new HashMap<>();
    
    static {
        HANDLERS.put(LabelComputeNodeStatement.class, LabelComputeNodeHandler.class);
        HANDLERS.put(UnlabelComputeNodeStatement.class, UnlabelComputeNodeHandler.class);
        HANDLERS.put(SetInstanceStatusStatement.class, SetInstanceStatusHandler.class);
        HANDLERS.put(SetDistVariableStatement.class, SetDistVariableHandler.class);
        HANDLERS.put(AlterReadwriteSplittingStorageUnitStatusStatement.class, AlterReadwriteSplittingStorageUnitStatusStatementHandler.class);
        HANDLERS.put(RefreshDatabaseMetadataStatement.class, RefreshDatabaseMetadataHandler.class);
        HANDLERS.put(RefreshTableMetadataStatement.class, RefreshTableMetadataHandler.class);
        HANDLERS.put(PrepareDistSQLStatement.class, PrepareDistSQLHandler.class);
        HANDLERS.put(ApplyDistSQLStatement.class, ApplyDistSQLHandler.class);
        HANDLERS.put(DiscardDistSQLStatement.class, DiscardDistSQLHandler.class);
        HANDLERS.put(ImportDatabaseConfigurationStatement.class, ImportDatabaseConfigurationHandler.class);
        HANDLERS.put(ShowComputeNodesStatement.class, ShowComputeNodesHandler.class);
        HANDLERS.put(ShowDistVariableStatement.class, ShowDistVariableHandler.class);
        HANDLERS.put(ShowDistVariablesStatement.class, ShowDistVariablesHandler.class);
        HANDLERS.put(ShowStatusFromReadwriteSplittingRulesStatement.class, ShowStatusFromReadwriteSplittingRulesHandler.class);
        HANDLERS.put(ShowTableMetadataStatement.class, ShowTableMetadataHandler.class);
        HANDLERS.put(ExportDatabaseConfigurationStatement.class, ExportDatabaseConfigurationHandler.class);
        HANDLERS.put(ConvertYamlConfigurationStatement.class, ConvertYamlConfigurationHandler.class);
        HANDLERS.put(ShowComputeNodeInfoStatement.class, ShowComputeNodeInfoHandler.class);
        HANDLERS.put(ShowComputeNodeModeStatement.class, ShowComputeNodeModeHandler.class);
    }
    
    /**
     * Create new instance of RAL backend handler.
     *
     * @param sqlStatement RAL statement
     * @param connectionSession connection session
     * @return created instance
     */
    public static ProxyBackendHandler newInstance(final RALStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof HintRALStatement) {
            return new HintRALBackendHandler((HintRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof QueryableScalingRALStatement) {
            DistSQLResultSet resultSet = QueryableScalingRALBackendHandlerFactory.newInstance((QueryableScalingRALStatement) sqlStatement);
            return new QueryableScalingRALBackendHandler((QueryableScalingRALStatement) sqlStatement, (DatabaseDistSQLResultSet) resultSet);
        }
        if (sqlStatement instanceof UpdatableScalingRALStatement) {
            return new UpdatableScalingRALBackendHandler((UpdatableScalingRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof QueryableGlobalRuleRALStatement) {
            return QueryableGlobalRuleRALBackendHandlerFactory.newInstance((QueryableGlobalRuleRALStatement) sqlStatement);
        }
        if (sqlStatement instanceof UpdatableGlobalRuleRALStatement) {
            return UpdatableGlobalRuleRALBackendHandlerFactory.newInstance((UpdatableGlobalRuleRALStatement) sqlStatement);
        }
        return createRALBackendHandler(sqlStatement, connectionSession);
    }
    
    private static RALBackendHandler<?> newInstance(final Class<? extends RALBackendHandler<?>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new UnsupportedSQLOperationException(String.format("Can not find public constructor for class `%s`", clazz.getName()));
        }
    }
    
    private static RALBackendHandler<?> createRALBackendHandler(final RALStatement sqlStatement, final ConnectionSession connectionSession) {
        Class<? extends RALBackendHandler<?>> clazz = HANDLERS.get(sqlStatement.getClass());
        ShardingSpherePreconditions.checkState(null != clazz, () -> new UnsupportedSQLOperationException(String.format("Unsupported SQL statement : %s", sqlStatement.getClass().getCanonicalName())));
        RALBackendHandler<?> result = newInstance(clazz);
        result.init(sqlStatement, connectionSession);
        return result;
    }
}
