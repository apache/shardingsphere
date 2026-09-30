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

package org.apache.shardingsphere.sqlfederation.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderDuplicatedException;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * SQL federation provider factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationProviderFactory {
    
    /**
     * Get SQL federation provider without calling SQLFederationProvider.initialize.
     *
     * @param providerType provider type, or null to select the default provider
     * @return selected provider
     */
    public static SQLFederationProvider getProvider(final String providerType) {
        Set<String> types = new HashSet<>();
        for (SQLFederationProvider each : ShardingSphereServiceLoader.getServiceInstances(SQLFederationProvider.class)) {
            ShardingSpherePreconditions.checkState(types.add(each.getType().toUpperCase(Locale.ROOT)), () -> new SQLFederationProviderDuplicatedException(each.getType()));
        }
        return TypedSPILoader.getService(SQLFederationProvider.class, providerType);
    }
}
