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

package org.apache.shardingsphere.authority.provider.schema;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.schema.builder.SchemaPrivilegeBuilder;
import org.apache.shardingsphere.authority.spi.AuthorityProvideAlgorithm;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

public final class SchemaPrivilegesPermittedAuthorityProviderAlgorithm implements AuthorityProvideAlgorithm {
    
    public static final String PROP_USER_SCHEMA_MAPPINGS = "user-schema-mappings";

    private final Map<ShardingSphereUser, ShardingSpherePrivileges> userPrivilegeMap = new ConcurrentHashMap<>();

    private Properties props;

    @Override
    public void setProps(final Properties props) {
        this.props = props;
    }

    @Override
    public Properties getProps() {
        return this.props;
    }

    @Override
    public void init(final Map<String, ShardingSphereMetaData> mataDataMap, final Collection<ShardingSphereUser> users) {
        this.userPrivilegeMap.putAll(SchemaPrivilegeBuilder.build(users, props));
    }

    @Override
    public void refresh(final Map<String, ShardingSphereMetaData> mataDataMap, final Collection<ShardingSphereUser> users) {
        this.userPrivilegeMap.putAll(SchemaPrivilegeBuilder.build(users, props));
    }

    @Override
    public Optional<ShardingSpherePrivileges> findPrivileges(final Grantee grantee) {
        return userPrivilegeMap.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst().map(userPrivilegeMap::get);
    }

    @Override
    public String getType() {
        return "SCHEMA_PRIVILEGES_PERMITTED";
    }
}
