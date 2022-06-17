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

import com.google.common.base.Splitter;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterInstanceStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.XaRecoveryIdChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;

import java.util.Optional;

/**
 * Alter instance handler.
 */
public final class AlterInstanceHandler extends UpdatableRALBackendHandler<AlterInstanceStatement> {
    
    private static final String XA_RECOVERY_NODES = "xa_recovery_nodes";
    
    @Override
    protected void update(final ContextManager contextManager) throws DistSQLException {
        if (!XA_RECOVERY_NODES.equalsIgnoreCase(getSqlStatement().getKey())) {
            throw new UnsupportedOperationException(String.format("%s is not supported", getSqlStatement().getKey()));
        }
        Optional<MetaDataPersistService> persistService = contextManager.getMetaDataContexts().getPersistService();
        if (!persistService.isPresent()) {
            throw new UnsupportedOperationException(String.format("No persistence configuration found, unable to set '%s'", getSqlStatement().getKey()));
        }
        if (!contextManager.getInstanceContext().getComputeNodeInstanceById(getSqlStatement().getInstanceId()).isPresent()) {
            throw new UnsupportedOperationException(String.format("'%s' does not exist", getSqlStatement().getInstanceId()));
        }
        // TODO need support standalone mode
        ShardingSphereEventBus.getInstance().post(new XaRecoveryIdChangedEvent(getSqlStatement().getInstanceId(), Splitter.on(",").splitToList(getSqlStatement().getValue())));
    }
}
