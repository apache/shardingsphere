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

package org.apache.shardingsphere.orchestration.core.common;

import org.apache.shardingsphere.orchestration.center.exception.OrchestrationException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CenterTypeTest {
    
    @Test
    public void assertCenterType() {
        assertThat(CenterType.CONFIG_CENTER.getValue(), is("config_center"));
        assertThat(CenterType.CONFIG_CENTER.name(), is("CONFIG_CENTER"));
        assertThat(CenterType.REGISTRY_CENTER.getValue(), is("registry_center"));
        assertThat(CenterType.REGISTRY_CENTER.name(), is("REGISTRY_CENTER"));
        assertThat(CenterType.METADATA_CENTER.getValue(), is("metadata_center"));
        assertThat(CenterType.METADATA_CENTER.name(), is("METADATA_CENTER"));
    }
    
    @Test
    public void assertFindByValue() {
        CenterType configCenter = CenterType.findByValue("config_center");
        assertThat(configCenter, is(CenterType.CONFIG_CENTER));
        CenterType registryCenter = CenterType.findByValue("registry_center");
        assertThat(registryCenter, is(CenterType.REGISTRY_CENTER));
        CenterType metadataCenter = CenterType.findByValue("metadata_center");
        assertThat(metadataCenter, is(CenterType.METADATA_CENTER));
    }
    
    @Test(expected = OrchestrationException.class)
    public void assertFindByValueException() {
        CenterType.findByValue("myTest");
    }
}
