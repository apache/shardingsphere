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

package org.apache.shardingsphere.test.e2e.discovery.cases.mysql;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.discovery.cases.DatabaseClusterEnvironment;
import org.apache.shardingsphere.test.e2e.discovery.cases.DatabaseClusterEnvironmentFactory;
import org.apache.shardingsphere.test.e2e.discovery.cases.DiscoveryContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.cases.DiscoveryTestAction;
import org.apache.shardingsphere.test.e2e.discovery.env.DiscoveryE2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.discovery.framework.parameter.DiscoveryTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.SQLException;
import java.util.stream.Stream;

public final class MySQLMGRDiscoveryE2EIT {
    
    private static final MySQLDatabaseType DATABASE_TYPE = new MySQLDatabaseType();
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertDiscoveryMySQLMGR(final DiscoveryTestParameter testParam) throws SQLException {
        try (DiscoveryContainerComposer composer = new DiscoveryContainerComposer(testParam)) {
            DatabaseClusterEnvironment databaseClusterEnv = DatabaseClusterEnvironmentFactory.newInstance("MySQL.MGR", composer.getMappedDataSources());
            new DiscoveryTestAction(composer, databaseClusterEnv).execute();
        }
    }
    
    private static boolean isEnabled() {
        return !DiscoveryE2ETestEnvironment.getInstance().listStorageContainerImages(DATABASE_TYPE).isEmpty();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return DiscoveryE2ETestEnvironment.getInstance().listStorageContainerImages(DATABASE_TYPE)
                    .stream().map(each -> Arguments.of(new DiscoveryTestParameter(DATABASE_TYPE, each, "mgr_discovery")));
        }
    }
}
