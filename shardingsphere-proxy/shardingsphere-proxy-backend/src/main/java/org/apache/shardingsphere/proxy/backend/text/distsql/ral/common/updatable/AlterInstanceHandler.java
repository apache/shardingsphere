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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterInstanceStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter instance handler.
 */
public final class AlterInstanceHandler extends UpdatableRALBackendHandler<AlterInstanceStatement, AlterInstanceHandler> {
    
    private static final String XA_RECOVERY_ID = "xa_recovery_id";
    
    @Override
    protected void update(final ContextManager contextManager, final AlterInstanceStatement sqlStatement) throws DistSQLException {
        if (XA_RECOVERY_ID.equalsIgnoreCase(sqlStatement.getKey())) {
            setXaRecoveryId(contextManager, sqlStatement);
        } else {
            throw new UnsupportedOperationException(String.format("%s is not supported", sqlStatement.getKey()));
        }
    }
    
    private void setXaRecoveryId(final ContextManager contextManager, final AlterInstanceStatement sqlStatement) {
        Optional<MetaDataPersistService> persistService = contextManager.getMetaDataContexts().getMetaDataPersistService();
        if (!persistService.isPresent()) {
            throw new UnsupportedOperationException(String.format("No persistence configuration found, unable to set '%s'", sqlStatement.getKey()));
        }
        Collection<ComputeNodeInstance> instances = persistService.get().getComputeNodePersistService().loadAllComputeNodeInstances();
        checkExisted(instances, sqlStatement);
        checkDuplicated(instances, sqlStatement);
        persistService.get().getComputeNodePersistService().persistInstanceXaRecoveryId(sqlStatement.getInstanceId(), sqlStatement.getValue());
    }
    
    private void checkExisted(final Collection<ComputeNodeInstance> instances, final AlterInstanceStatement sqlStatement) {
        Collection<String> instanceIds = instances.stream().map(each -> each.getInstanceDefinition().getInstanceId().getId()).collect(Collectors.toSet());
        if (!instanceIds.contains(sqlStatement.getInstanceId())) {
            throw new UnsupportedOperationException(String.format("'%s' does not exist", sqlStatement.getValue()));
        }
    }
    
    private void checkDuplicated(final Collection<ComputeNodeInstance> instances, final AlterInstanceStatement sqlStatement) {
        Collection<String> invalid = instances.stream().filter(each -> each.getXaRecoveryId().equals(sqlStatement.getValue()))
                .map(each -> each.getInstanceDefinition().getInstanceId().getId()).filter(each -> !each.equals(sqlStatement.getInstanceId())).collect(Collectors.toSet());
        if (!invalid.isEmpty()) {
            throw new UnsupportedOperationException(String.format("'%s' is already used by %s", sqlStatement.getValue(), invalid));
        }
    }
}
