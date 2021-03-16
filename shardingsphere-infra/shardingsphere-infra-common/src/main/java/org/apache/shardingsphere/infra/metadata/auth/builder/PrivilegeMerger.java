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

package org.apache.shardingsphere.infra.metadata.auth.builder;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Map;

/**
 * Privilege merger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeMerger {
    
    /**
     * Merge.
     * 
     * @param authentication authentication
     * @param schemaName schema name
     * @param rules ShardingSphere rules
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivilege> merge(final Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> authentication,
                                                                            final String schemaName, final Collection<ShardingSphereRule> rules) {
        // TODO :merge by rules
        return Maps.transformEntries(authentication, (key, value) -> merge(value));
    }
    
    private static ShardingSpherePrivilege merge(final Collection<ShardingSpherePrivilege> privileges) {
        return privileges.isEmpty() ? new ShardingSpherePrivilege() : privileges.iterator().next();
    }
}
