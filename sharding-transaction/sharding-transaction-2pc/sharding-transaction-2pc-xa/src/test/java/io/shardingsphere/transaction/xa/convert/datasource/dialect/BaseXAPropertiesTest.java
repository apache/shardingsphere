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

package io.shardingsphere.transaction.xa.convert.datasource.dialect;

import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.Getter;
import org.junit.Before;

@Getter
public abstract class BaseXAPropertiesTest {
    
    private DataSourceParameter dataSourceParameter;
    
    @Before
    public void setUp() {
        dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setMaxPoolSize(100);
        dataSourceParameter.setConnectionTimeoutMilliseconds(1000);
        dataSourceParameter.setIdleTimeoutMilliseconds(1000);
        dataSourceParameter.setMaxLifetimeMilliseconds(60000);
    }
}
