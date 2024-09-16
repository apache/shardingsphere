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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.config.AlterPropertiesEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesDispatchEventBuilderTest {
    
    private final PropertiesDispatchEventBuilder builder = new PropertiesDispatchEventBuilder();
    
    @Test
    void assertGetSubscribedKey() {
        assertThat(builder.getSubscribedKey(), is("/props"));
    }
    
    @Test
    void assertBuildAlterPropertiesEvent() {
        Optional<AlterPropertiesEvent> actual = builder.build(new DataChangedEvent("/props/active_version", "key=value", Type.ADDED));
        assertTrue(actual.isPresent());
        assertThat((actual.get()).getActiveVersionKey(), is("/props/active_version"));
        assertThat((actual.get()).getActiveVersion(), is("key=value"));
    }
    
    @Test
    void assertBuildWithInvalidEventKey() {
        Optional<AlterPropertiesEvent> actual = builder.build(new DataChangedEvent("/props/xxx", "key=value", Type.ADDED));
        assertFalse(actual.isPresent());
    }
}
