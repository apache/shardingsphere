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

package org.apache.shardingsphere.infra.yaml.config.algorithm;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.fixture.TypedSPIFixture;
import org.apache.shardingsphere.infra.spi.fixture.TypedSPIFixtureImpl;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlShardingSphereAlgorithmConfigurationTest {
    
    @Test
    public void assertBuildByTypedSPI() {
        ShardingSphereServiceLoader.register(TypedSPIFixture.class);
        Collection<TypedSPIFixture> spiFixtures = ShardingSphereServiceLoader.newServiceInstances(TypedSPIFixture.class);
        Optional<TypedSPIFixture> spiOptional = spiFixtures.stream().filter(echo -> echo instanceof TypedSPIFixtureImpl).findFirst();
        assertTrue(spiOptional.isPresent());
        TypedSPIFixture typedSPIFixture = spiOptional.get();
        typedSPIFixture.setProps(createProps());
        YamlShardingSphereAlgorithmConfiguration actual = YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(typedSPIFixture);
        assertThat(actual.getType(), is("FIXTURE"));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    private Properties createProps() {
        Properties result = new Properties();
        result.setProperty("key", "value");
        return result;
    }
}
