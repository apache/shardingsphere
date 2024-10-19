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

package org.apache.shardingsphere.data.pipeline.core.job.progress.persist;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PipelineJobProgressPersistServiceTest {
    
    @Test
    void assertAdd() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        Map<String, Map<Integer, PipelineJobProgressPersistContext>> jobProgressPersistMap = getJobProgressPersistMap();
        assertThat(jobProgressPersistMap.get("foo_id").get(1).getUnhandledEventCount().get(), is(0L));
    }
    
    @Test
    void assertRemove() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        PipelineJobProgressPersistService.remove("foo_id");
        Map<String, Map<Integer, PipelineJobProgressPersistContext>> jobProgressPersistMap = getJobProgressPersistMap();
        assertFalse(jobProgressPersistMap.containsKey("foo_id"));
    }
    
    @Test
    void assertNotifyPersist() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        PipelineJobProgressPersistService.notifyPersist("foo_id", 1);
        Map<String, Map<Integer, PipelineJobProgressPersistContext>> jobProgressPersistMap = getJobProgressPersistMap();
        assertThat(jobProgressPersistMap.get("foo_id").get(1).getUnhandledEventCount().get(), is(1L));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<String, Map<Integer, PipelineJobProgressPersistContext>> getJobProgressPersistMap() {
        Field field = PipelineJobProgressPersistService.class.getDeclaredField("JOB_PROGRESS_PERSIST_MAP");
        return (Map<String, Map<Integer, PipelineJobProgressPersistContext>>) Plugins.getMemberAccessor().get(field, PipelineJobProgressPersistService.class);
    }
    
    @Test
    void assertPersistNow() {
        PipelineJobProgressPersistService.add("foo_id", 1);
        assertDoesNotThrow(() -> PipelineJobProgressPersistService.persistNow("foo_id", 1));
    }
}
