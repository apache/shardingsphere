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

package org.apache.shardingsphere.driver.governance.internal.datasource;

import org.apache.shardingsphere.governance.repository.api.config.RegistryCenterConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class GovernanceShardingSphereDataSourceTest {
    
    @Test
    public void assertInitializeGovernanceShardingSphereDataSource() throws SQLException {
        assertThat(new GovernanceShardingSphereDataSource(DefaultSchema.LOGIC_NAME, getModeConfiguration()).getConnection(), instanceOf(Connection.class));
    }
    
    private static ModeConfiguration getModeConfiguration() {
        return new ModeConfiguration("Cluster", getRegistryCenterConfiguration(), true);
    }
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        Properties properties = new Properties();
        properties.setProperty("overwrite", "true");
        return new RegistryCenterConfiguration("GOV_TEST", "test_name", "localhost:3181", properties);
    }
}
