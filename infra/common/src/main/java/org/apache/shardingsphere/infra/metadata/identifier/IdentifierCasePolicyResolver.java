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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;

import javax.sql.DataSource;

/**
 * Resolver of identifier case policy.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdentifierCasePolicyResolver {
    
    /**
     * Resolve protocol identifier case policy.
     *
     * @param protocolType protocol type
     * @param props configuration properties
     * @return identifier case policy set
     */
    public static IdentifierCasePolicySet resolveProtocol(final DatabaseType protocolType, final ConfigurationProperties props) {
        return resolve(protocolType, props, null);
    }
    
    /**
     * Resolve storage identifier case policy.
     *
     * @param storageType storage type
     * @param props configuration properties
     * @param dataSource storage data source
     * @return identifier case policy set
     */
    public static IdentifierCasePolicySet resolveStorage(final DatabaseType storageType, final ConfigurationProperties props, final DataSource dataSource) {
        return resolve(storageType, props, dataSource);
    }
    
    private static IdentifierCasePolicySet resolve(final DatabaseType databaseType, final ConfigurationProperties props, final DataSource dataSource) {
        MetadataIdentifierCaseSensitivity configuredCaseSensitivity = new TemporaryConfigurationProperties(props.getProps())
                .getValue(TemporaryConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY);
        if (MetadataIdentifierCaseSensitivity.INSENSITIVE == configuredCaseSensitivity) {
            return IdentifierCasePolicyFactory.newInsensitivePolicySet();
        }
        IdentifierCasePolicyProviderContext context = new IdentifierCasePolicyProviderContext(databaseType, dataSource);
        return DatabaseTypedSPILoader.findService(IdentifierCasePolicyProvider.class, databaseType)
                .map(each -> each.provide(context))
                .orElseGet(IdentifierCasePolicyFactory::newInsensitivePolicySet);
    }
}
