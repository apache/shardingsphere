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

package org.apache.shardingsphere.mode.manager.standalone.persist.facade;

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacadeBuilder;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class StandalonePersistServiceFacadeBuilderTest {
    
    private final ModePersistServiceFacadeBuilder builder = TypedSPILoader.getService(ModePersistServiceFacadeBuilder.class, "Standalone");
    
    @Test
    void assertBuild() {
        assertThat(builder.build(mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS), mock(PersistRepository.class)), isA(StandalonePersistServiceFacade.class));
    }
}
