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

package org.apache.shardingsphere.governance.core.registry.listener.builder.impl;

import org.apache.shardingsphere.governance.core.registry.listener.builder.GovernanceListenerBuilder;
import org.apache.shardingsphere.governance.core.registry.listener.impl.LockChangedListener;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;

import java.util.Arrays;
import java.util.Collection;

/**
 * Lock changed listener builder.
 */
public final class LockChangedListenerBuilder implements GovernanceListenerBuilder {
    
    @Override
    public LockChangedListener create(final RegistryCenterRepository repository, final Collection<String> schemaNames) {
        return new LockChangedListener(repository);
    }
    
    @Override
    public Collection<Type> getWatchTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
}
