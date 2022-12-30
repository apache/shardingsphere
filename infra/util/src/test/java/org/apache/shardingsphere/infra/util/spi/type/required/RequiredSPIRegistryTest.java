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

package org.apache.shardingsphere.infra.util.spi.type.required;

import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.empty.EmptyRequiredSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.multiple.MultipleWithDefaultRequiredSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.multiple.MultipleWithoutDefaultRequiredSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.multiple.impl.BarNotDefaultMultipleRequiredSPIFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.multiple.impl.DefaultMultipleRequiredSPIFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.multiple.impl.FooNotDefaultMultipleRequiredSPIFixtureImpl;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.single.SingleRequiredSPIFixture;
import org.apache.shardingsphere.infra.util.spi.type.required.fixture.single.impl.SingleRequiredSPIFixtureImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RequiredSPIRegistryTest {
    
    @Test(expected = ServiceProviderNotFoundServerException.class)
    public void assertRegisteredServiceWithEmptyImplementation() {
        RequiredSPIRegistry.getRegisteredService(EmptyRequiredSPIFixture.class);
    }
    
    @Test
    public void assertRegisteredServiceWithOneImplementation() {
        SingleRequiredSPIFixture actual = RequiredSPIRegistry.getRegisteredService(SingleRequiredSPIFixture.class);
        assertThat(actual, instanceOf(SingleRequiredSPIFixtureImpl.class));
        assertTrue(((SingleRequiredSPIFixtureImpl) actual).isInitialized());
    }
    
    @Test
    public void assertRegisteredServiceWithMoreImplementationsAndWithDefaultImplementation() {
        assertThat(RequiredSPIRegistry.getRegisteredService(MultipleWithDefaultRequiredSPIFixture.class), instanceOf(DefaultMultipleRequiredSPIFixtureImpl.class));
    }
    
    @Test
    public void assertRegisteredServiceWithMoreImplementationsAndWithoutDefaultImplementation() {
        MultipleWithoutDefaultRequiredSPIFixture actual = RequiredSPIRegistry.getRegisteredService(MultipleWithoutDefaultRequiredSPIFixture.class);
        assertTrue(actual instanceof FooNotDefaultMultipleRequiredSPIFixtureImpl || actual instanceof BarNotDefaultMultipleRequiredSPIFixtureImpl);
    }
}
