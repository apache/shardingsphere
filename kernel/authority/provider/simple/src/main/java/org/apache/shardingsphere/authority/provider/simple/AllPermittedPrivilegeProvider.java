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

package org.apache.shardingsphere.authority.provider.simple;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.metadata.user.Grantee;

import java.util.Collection;
import java.util.Collections;

/**
 * All permitted privilege provider.
 *
 * @deprecated Use DatabasePermittedPrivilegeProvider instead.
 */
@Deprecated
public final class AllPermittedPrivilegeProvider implements PrivilegeProvider {
    
    @Override
    public ShardingSpherePrivileges build(final AuthorityRuleConfiguration ruleConfig, final Grantee grantee) {
        return new AllPermittedPrivileges();
    }
    
    @Override
    public String getType() {
        return "ALL_PERMITTED";
    }
    
    @Override
    public Collection<Object> getTypeAliases() {
        return Collections.singleton("ALL_PRIVILEGES_PERMITTED");
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
