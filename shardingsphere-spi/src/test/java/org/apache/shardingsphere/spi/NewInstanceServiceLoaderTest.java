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

package org.apache.shardingsphere.spi;

import org.apache.shardingsphere.spi.fixture.TypeBasedSPIFixture;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class NewInstanceServiceLoaderTest {
    
    @Test
    public void assertNewServiceInstanceWhenIsNotExist() {
        NewInstanceServiceLoader.register(Collection.class);
        Collection collection = NewInstanceServiceLoader.newServiceInstances(Collection.class);
        assertTrue(collection.isEmpty());
    }
    
    @Test
    public void assertNewServiceInstanceWhenIsExist() {
        NewInstanceServiceLoader.register(TypeBasedSPIFixture.class);
        Collection collection = NewInstanceServiceLoader.newServiceInstances(TypeBasedSPIFixture.class);
        assertThat(collection.size(), is(1));
    }
}
