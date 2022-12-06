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
import org.apache.shardingsphere.test.e2e.discovery.cases.base.BaseDiscoveryE2EIT;
import org.apache.shardingsphere.test.e2e.discovery.framework.parameter.DiscoveryTestParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

@RunWith(Parameterized.class)
public final class MGRGeneralDiscoveryE2EIT extends BaseDiscoveryE2EIT {
    
    public MGRGeneralDiscoveryE2EIT(final DiscoveryTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<DiscoveryTestParameter> getTestParameters() {
        Collection<DiscoveryTestParameter> result = new LinkedList<>();
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String each : ENV.listStorageContainerImages(databaseType)) {
            result.add(new DiscoveryTestParameter(databaseType, each, "mgr_discovery"));
        }
        return result;
    }
    
    @Test
    public void assertDiscoveryMySQLMGR() throws SQLException {
        DatabaseClusterEnvironment mgrEnvironment = DatabaseClusterEnvironmentFactory.newInstance("MySQL.MGR", getMappedDataSources());
        initDiscoveryEnvironment();
        assertClosePrimaryDataSource(mgrEnvironment);
        assertCloseReplicationDataSource(mgrEnvironment);
        assertCloseAllReplicationDataSource(mgrEnvironment);
    }
}
