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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.user.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.user.node.UserStatusNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * User status service.
 */
@RequiredArgsConstructor
public final class UserStatusService {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Persist users.
     *
     * @param users users
     */
    public void persist(final Collection<ShardingSphereUser> users) {
        repository.persist(UserStatusNode.getPrivilegeNodePath(), YamlEngine.marshal(YamlUsersConfigurationConverter.convertYamlUserConfigurations(users)));
    }
}
