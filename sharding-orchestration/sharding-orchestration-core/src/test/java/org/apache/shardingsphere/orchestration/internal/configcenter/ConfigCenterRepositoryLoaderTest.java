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

package org.apache.shardingsphere.orchestration.internal.configcenter;

import org.apache.shardingsphere.orchestration.center.api.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.internal.configcenter.fixture.FirstTestConfigCenterRepository;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class ConfigCenterRepositoryLoaderTest {
    
    @Test
    public void assertLoad() {
        ConfigCenterRepository configCenterRepository = new ConfigCenterServiceLoader().load(new InstanceConfiguration("FirstTestConfigCenter"));
        assertThat(configCenterRepository, instanceOf(FirstTestConfigCenterRepository.class));
    }
}
