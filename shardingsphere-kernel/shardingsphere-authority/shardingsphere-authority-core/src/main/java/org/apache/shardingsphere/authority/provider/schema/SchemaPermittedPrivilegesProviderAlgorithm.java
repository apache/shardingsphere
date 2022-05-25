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

import lombok.Getter;
import org.apache.shardingsphere.authority.model.AuthorityRegistry;
import org.apache.shardingsphere.authority.provider.schema.builder.SchemaPrivilegeBuilder;
import org.apache.shardingsphere.authority.registry.UserPrivilegeMapAuthorityRegistry;
import org.apache.shardingsphere.authority.spi.AuthorityProviderAlgorithm;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Schema permitted privileges provider algorithm.
 */
public final class SchemaPermittedPrivilegesProviderAlgorithm implements AuthorityProviderAlgorithm {
    
    public static final String PROP_USER_SCHEMA_MAPPINGS = "user-schema-mappings";
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @Override
    public AuthorityRegistry buildAuthorityRegistry(final Map<String, ShardingSphereDatabase> databaseMap, final Collection<ShardingSphereUser> users) {
        return new UserPrivilegeMapAuthorityRegistry(SchemaPrivilegeBuilder.build(users, props));
    }
    
    @Override
    public String getType() {
        return "SCHEMA_PERMITTED";
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return Collections.singleton("SCHEMA_PRIVILEGES_PERMITTED");
    }
}
