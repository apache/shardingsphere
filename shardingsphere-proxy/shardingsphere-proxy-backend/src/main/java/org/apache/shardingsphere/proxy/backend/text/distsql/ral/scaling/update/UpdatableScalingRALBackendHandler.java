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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.scaling.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.infra.distsql.update.RALUpdater;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

/**
 * Updatable scaling RAL backend handler factory.
 */
@Setter
public final class UpdatableScalingRALBackendHandler implements TextProtocolBackendHandler {
    
    static {
        ShardingSphereServiceLoader.register(RALUpdater.class);
    }
    
    private UpdatableScalingRALStatement sqlStatement;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ResponseHeader execute() {
        RALUpdater updater = TypedSPIRegistry.getRegisteredService(RALUpdater.class, sqlStatement.getClass().getCanonicalName());
        updater.executeUpdate(sqlStatement);
        return new UpdateResponseHeader(sqlStatement);
    }
}
