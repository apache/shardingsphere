/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.jdbc.orche.config;

import io.shardingsphere.example.config.ExampleConfiguration;
import io.shardingsphere.example.jdbc.orche.config.regcenter.RegistryCenterExampleConfiguration;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

public abstract class OrchestrationExampleConfiguration implements ExampleConfiguration {
    
    private final RegistryCenterExampleConfiguration registryCenterExampleConfig;
    
    private final boolean loadConfigFromRegCenter;
    
    public OrchestrationExampleConfiguration(final RegistryCenterExampleConfiguration registryCenterExampleConfig, final boolean loadConfigFromRegCenter) {
        this.registryCenterExampleConfig = registryCenterExampleConfig;
        this.loadConfigFromRegCenter = loadConfigFromRegCenter;
    }
    
    @Override
    public final DataSource getDataSource() throws SQLException {
        return loadConfigFromRegCenter ? getDataSourceFromRegCenter() : getDataSourceFromLocalConfiguration();
    }
    
    protected abstract DataSource getDataSourceFromRegCenter() throws SQLException;
    
    protected abstract DataSource getDataSourceFromLocalConfiguration() throws SQLException;
    
    protected final RegistryCenterConfiguration getRegistryCenterConfiguration() {
        return registryCenterExampleConfig.getConfiguration();
    }
}
