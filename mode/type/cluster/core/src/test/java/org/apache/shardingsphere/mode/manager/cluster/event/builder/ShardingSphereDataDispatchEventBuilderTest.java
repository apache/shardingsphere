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
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.TableDataChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShardingSphereDataDispatchEventBuilderTest {
    
    private final ShardingSphereDataDispatchEventBuilder builder = new ShardingSphereDataDispatchEventBuilder();
    
    @Test
    void assertGetSubscribedKey() {
        assertThat(builder.getSubscribedKey(), is("/statistics/databases"));
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertBuild(final String name, final String eventKey, final String eventValue, final DataChangedEvent.Type type, final boolean isEventPresent, final Consumer<DispatchEvent> consumer) {
        Optional<DispatchEvent> actual = builder.build(new DataChangedEvent(eventKey, eventValue, type));
        assertThat(actual.isPresent(), is(isEventPresent));
        if (actual.isPresent() && consumer != null) {
            consumer.accept(actual.get());
        }
    }
    
    static void assertBuildDatabaseDataChangedEventWithAdd(final DispatchEvent actual) {
        assertThat(((DatabaseDataAddedEvent) actual).getDatabaseName(), is("foo_db"));
    }
    
    static void assertBuildDatabaseDataChangedEventWithUpdate(final DispatchEvent actual) {
        assertThat(((DatabaseDataAddedEvent) actual).getDatabaseName(), is("foo_db"));
    }
    
    static void assertBuildDatabaseDataChangedEventWithDelete(final DispatchEvent actual) {
        assertThat(((DatabaseDataDeletedEvent) actual).getDatabaseName(), is("foo_db"));
    }
    
    static void assertBuildSchemaDataADDEDEvent(final DispatchEvent actual) {
        assertThat(((SchemaDataAddedEvent) actual).getDatabaseName(), is("foo_db"));
        assertThat(((SchemaDataAddedEvent) actual).getSchemaName(), is("foo_schema"));
    }
    
    static void assertBuildSchemaDataChangedEventWithDelete(final DispatchEvent actual) {
        assertThat(((SchemaDataDeletedEvent) actual).getDatabaseName(), is("foo_db"));
        assertThat(((SchemaDataDeletedEvent) actual).getSchemaName(), is("foo_schema"));
    }
    
    static void assertBuildRowDataChangedEventWithDelete(final DispatchEvent actual) {
        assertThat(((ShardingSphereRowDataDeletedEvent) actual).getDatabaseName(), is("foo_db"));
        assertThat(((ShardingSphereRowDataDeletedEvent) actual).getSchemaName(), is("foo_schema"));
        assertThat(((ShardingSphereRowDataDeletedEvent) actual).getTableName(), is("foo_tbl"));
        assertThat(((ShardingSphereRowDataDeletedEvent) actual).getUniqueKey(), is("1"));
    }
    
    static void assertBuildRowDataChangedEvent(final DispatchEvent actual) {
        assertThat(((ShardingSphereRowDataChangedEvent) actual).getDatabaseName(), is("foo_db"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual).getSchemaName(), is("foo_schema"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual).getTableName(), is("foo_tbl"));
        assertThat(((ShardingSphereRowDataChangedEvent) actual).getYamlRowData().getUniqueKey(), is("1"));
    }
    
    static void assertBuildTableDataChangedEventWithDelete(final DispatchEvent actual) {
        assertThat(((TableDataChangedEvent) actual).getDatabaseName(), is("foo_db"));
        assertThat(((TableDataChangedEvent) actual).getSchemaName(), is("foo_schema"));
        assertNull(((TableDataChangedEvent) actual).getAddedTable());
        assertThat(((TableDataChangedEvent) actual).getDeletedTable(), is("foo_tbl"));
    }
    
    static void assertBuildTableDataChangedEvent(final DispatchEvent actual) {
        assertThat(((TableDataChangedEvent) actual).getDatabaseName(), is("foo_db"));
        assertThat(((TableDataChangedEvent) actual).getSchemaName(), is("foo_schema"));
        assertThat(((TableDataChangedEvent) actual).getAddedTable(), is("foo_tbl"));
        assertNull(((TableDataChangedEvent) actual).getDeletedTable());
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("assertBuildDatabaseDataChangedEventWithAdd", "/statistics/databases/foo_db", "", Type.ADDED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildDatabaseDataChangedEventWithAdd),
                    Arguments.of("assertBuildDatabaseDataChangedEventWithUpdate", "/statistics/databases/foo_db", "", Type.UPDATED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildDatabaseDataChangedEventWithUpdate),
                    Arguments.of("assertBuildDatabaseDataChangedEventWithDelete", "/statistics/databases/foo_db", "", Type.DELETED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildDatabaseDataChangedEventWithDelete),
                    Arguments.of("assertBuildSchemaDataChangedEventWithAdd", "/statistics/databases/foo_db/schemas/foo_schema", "", Type.ADDED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildSchemaDataADDEDEvent),
                    Arguments.of("assertBuildSchemaDataChangedEventWithUpdate", "/statistics/databases/foo_db/schemas/foo_schema", "", Type.UPDATED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildSchemaDataADDEDEvent),
                    Arguments.of("assertBuildSchemaDataChangedEventWithDelete", "/statistics/databases/foo_db/schemas/foo_schema", "", Type.DELETED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildSchemaDataChangedEventWithDelete),
                    Arguments.of("assertBuildRowDataChangedEventWithAddNullValue", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "", Type.ADDED, false, null),
                    Arguments.of("assertBuildWithMissedDatabaseNameEventKey", "/statistics/databases", "", Type.ADDED, false, null),
                    Arguments.of("assertBuildWithMissedSchemaNameEventKey", "/statistics/databases/foo_db/schemas", "", Type.ADDED, false, null),
                    Arguments.of("assertBuildWithMissedTableNameEventKey", "/statistics/databases/foo_db/schemas/foo_schema/tables", "", Type.ADDED, false, null),
                    Arguments.of("assertBuildDatabaseDataChangedEventWithIgnore", "/statistics/databases/foo_db", "", Type.IGNORED, false, null),
                    Arguments.of("assertBuildTableDataChangedEventWithIgnore", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.IGNORED, false, null),
                    Arguments.of("assertBuildSchemaDataChangedEventWithIgnore", "/statistics/databases/foo_db/schemas/foo_schema", "", Type.IGNORED, false, null),
                    Arguments.of("assertBuildWithMissedRowEventKey", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/", "", Type.UPDATED, false, null),
                    Arguments.of("assertBuildRowDataChangedEventWithDelete", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "", Type.DELETED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildRowDataChangedEventWithDelete),
                    Arguments.of("assertBuildRowDataChangedEventWithUpdate", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "{uniqueKey: 1}", Type.UPDATED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildRowDataChangedEvent),
                    Arguments.of("assertBuildRowDataChangedEventWithAdd", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/xxx", "{uniqueKey: 1}", Type.ADDED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildRowDataChangedEvent),
                    Arguments.of("assertBuildTableDataChangedEventWithDelete", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildTableDataChangedEventWithDelete),
                    Arguments.of("assertBuildTableDataChangedEventWithUpdate", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.UPDATED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildTableDataChangedEvent),
                    Arguments.of("assertBuildTableDataChangedEventWithAdd", "/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.ADDED, true,
                            (Consumer<DispatchEvent>) ShardingSphereDataDispatchEventBuilderTest::assertBuildTableDataChangedEvent));
        }
    }
}
