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

package org.apache.shardingsphere.shadow.event;

import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.shadow.event.algorithm.AlterDefaultShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.AlterShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DropDefaultShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DropShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.datasource.AlterShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.datasource.DropShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.table.AlterShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.DropShadowTableEvent;
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowRuleNodePathProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShadowRuleChangedEventCreatorTest {
    
    private ShadowRuleChangedEventCreator shadowRuleChangedEventCreator;
    
    @BeforeEach
    void setUp() {
        shadowRuleChangedEventCreator = new ShadowRuleChangedEventCreator();
    }
    
    @Test
    void assertNamedRuleItemChangedDataSourceCreatorEvent() {
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                ShadowRuleNodePathProvider.DATA_SOURCES, "test"), instanceOf(DropShadowDataSourceEvent.class));
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.ADDED),
                ShadowRuleNodePathProvider.DATA_SOURCES, "test"), instanceOf(AlterShadowDataSourceEvent.class));
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.UPDATED),
                ShadowRuleNodePathProvider.DATA_SOURCES, "test"), instanceOf(AlterShadowDataSourceEvent.class));
    }
    
    @Test
    void assertNamedRuleItemChangedAlgorithmCreatorEvent() {
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                ShadowRuleNodePathProvider.ALGORITHMS, "test"), instanceOf(DropShadowAlgorithmEvent.class));
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.ADDED),
                ShadowRuleNodePathProvider.ALGORITHMS, "test"), instanceOf(AlterShadowAlgorithmEvent.class));
    }
    
    @Test
    void assertNamedRuleItemChangedTableCreatorEvent() {
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                ShadowRuleNodePathProvider.TABLES, "test"), instanceOf(DropShadowTableEvent.class));
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.ADDED),
                ShadowRuleNodePathProvider.TABLES, "test"), instanceOf(AlterShadowTableEvent.class));
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.UPDATED),
                ShadowRuleNodePathProvider.TABLES, "test"), instanceOf(AlterShadowTableEvent.class));
    }
    
    @Test
    void assertNamedRuleItemChangedCreatorThrowsUnsupportedException() {
        assertThrows(UnsupportedOperationException.class, () -> shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                "Invalid Type", "test"));
    }
    
    @Test
    void assertUniqueRuleItemChangedDatasourceCreatorEvent() {
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED),
                ShadowRuleNodePathProvider.DATA_SOURCES), instanceOf(DropDefaultShadowAlgorithmEvent.class));
        assertThat(shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.UPDATED),
                ShadowRuleNodePathProvider.DATA_SOURCES), instanceOf(AlterDefaultShadowAlgorithmEvent.class));
    }
    
    @Test
    void assertUniqueRuleItemChangedCreatorThrowsUnsupportedException() {
        assertThrows(UnsupportedOperationException.class, () -> shadowRuleChangedEventCreator.create("test",
                new DataChangedEvent("RANDOM", "RANDOM", DataChangedEvent.Type.DELETED), "Invalid Type"));
    }
}
