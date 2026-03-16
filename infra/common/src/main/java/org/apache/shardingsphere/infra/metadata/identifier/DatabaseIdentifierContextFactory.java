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

package org.apache.shardingsphere.infra.metadata.identifier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSets;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database identifier context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseIdentifierContextFactory {
    
    /**
     * Create default identifier context.
     *
     * @return default identifier context
     */
    public static DatabaseIdentifierContext createDefault() {
        return new DatabaseIdentifierContext(IdentifierCaseRuleSets.newInsensitiveRuleSet());
    }
    
    /**
     * Create identifier context with protocol-aware identifier rules.
     *
     * @param protocolType protocol type
     * @param props configuration properties
     * @return identifier context
     */
    public static DatabaseIdentifierContext create(final DatabaseType protocolType, final ConfigurationProperties props) {
        return create(protocolType, getProps(props), null);
    }
    
    /**
     * Create identifier context with protocol-aware identifier rules.
     *
     * @param protocolType protocol type
     * @param resourceMetaData resource meta data
     * @param props configuration properties
     * @return identifier context
     */
    public static DatabaseIdentifierContext create(final DatabaseType protocolType, final ResourceMetaData resourceMetaData, final ConfigurationProperties props) {
        return create(protocolType, getProps(props), getFirstDataSource(resourceMetaData));
    }
    
    private static DatabaseIdentifierContext create(final DatabaseType protocolType, final ConfigurationProperties props, final DataSource dataSource) {
        return new DatabaseIdentifierContext(new IdentifierCaseRuleResolver().resolve(protocolType, props, dataSource));
    }
    
    private static ConfigurationProperties getProps(final ConfigurationProperties props) {
        return null == props ? new ConfigurationProperties(new Properties()) : props;
    }
    
    private static DataSource getFirstDataSource(final ResourceMetaData resourceMetaData) {
        if (null == resourceMetaData || null == resourceMetaData.getStorageUnits() || resourceMetaData.getStorageUnits().isEmpty()) {
            return null;
        }
        return resourceMetaData.getStorageUnits().values().iterator().next().getDataSource();
    }
}
