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

package org.apache.shardingsphere.authority.fixture;

import org.apache.shardingsphere.authority.model.AuthorityRegistry;
import org.apache.shardingsphere.authority.spi.AuthorityRegistryProvider;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Collection;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AuthorityRegistryProviderFixture implements AuthorityRegistryProvider {
    
    @Override
    public AuthorityRegistry build(final Collection<ShardingSphereUser> users) {
        AuthorityRegistry result = mock(AuthorityRegistry.class);
        when(result.findPrivileges(any())).thenReturn(Optional.of(new ShardingSpherePrivilegesFixture()));
        return result;
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
