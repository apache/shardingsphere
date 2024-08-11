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

package org.apache.shardingsphere.mode.event.builder;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropUniqueRuleItemEvent;
import org.apache.shardingsphere.mode.path.rule.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereServiceLoader.class)
class RuleConfigurationEventBuilderTest {
    
    @Test
    void assertBuildWithoutRuleNodePathProvider() {
        when(ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)).thenReturn(Collections.emptyList());
        assertFalse(new RuleConfigurationEventBuilder().build("foo_db", new DataChangedEvent("k", "v", Type.IGNORED)).isPresent());
    }
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertBuild(final String name, final String eventKey, final String eventValue, final DataChangedEvent.Type type,
                     final boolean isEventPresent, final Class<? extends DispatchEvent> dispatchEventClass) {
        RuleNodePathProvider ruleNodePathProvider = mock(RuleNodePathProvider.class, RETURNS_DEEP_STUBS);
        when(ruleNodePathProvider.getRuleNodePath()).thenReturn(new RuleNodePath("fixture", Collections.singleton("named"), Collections.singleton("unique")));
        when(ShardingSphereServiceLoader.getServiceInstances(RuleNodePathProvider.class)).thenReturn(Collections.singleton(ruleNodePathProvider));
        Optional<DispatchEvent> actual = new RuleConfigurationEventBuilder().build("foo_db", new DataChangedEvent(eventKey, eventValue, type));
        assertThat(actual.isPresent(), is(isEventPresent));
        if (actual.isPresent()) {
            if (dispatchEventClass == AlterNamedRuleItemEvent.class) {
                assertDispatchEvent((AlterNamedRuleItemEvent) actual.get());
            } else if (dispatchEventClass == DropNamedRuleItemEvent.class) {
                assertDispatchEvent((DropNamedRuleItemEvent) actual.get());
            } else if (dispatchEventClass == AlterUniqueRuleItemEvent.class) {
                assertDispatchEvent((AlterUniqueRuleItemEvent) actual.get());
            } else if (dispatchEventClass == DropUniqueRuleItemEvent.class) {
                assertDispatchEvent((DropUniqueRuleItemEvent) actual.get());
            } else {
                fail("No such event type.");
            }
        }
    }
    
    private void assertDispatchEvent(final AlterNamedRuleItemEvent actual) {
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getItemName(), is("xxx"));
        assertThat(actual.getType(), is("fixture.named"));
    }
    
    private void assertDispatchEvent(final DropNamedRuleItemEvent actual) {
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getItemName(), is("xxx"));
        assertThat(actual.getType(), is("fixture.named"));
    }
    
    private void assertDispatchEvent(final AlterUniqueRuleItemEvent actual) {
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getActiveVersionKey(), is("/metadata/fixture/rules/fixture/unique/active_version"));
        assertThat(actual.getActiveVersion(), is("foo"));
        assertThat(actual.getType(), is("fixture.unique"));
    }
    
    private void assertDispatchEvent(final DropUniqueRuleItemEvent actual) {
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getType(), is("fixture.unique"));
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(
                    Arguments.of("InvalidPath", "/metadata/invalid/rules/fixture", "foo", Type.ADDED, false, null),
                    Arguments.of("AddEventWithEmptyValue", "/metadata/fixture/rules/fixture/versions/0", "", Type.ADDED, false, null),
                    Arguments.of("PathNotFound", "/metadata/fixture/rules/fixture/versions/0", "foo", Type.ADDED, false, null),
                    Arguments.of("AddEventWithNamedRuleItemNodePath", "/metadata/fixture/rules/fixture/named/xxx/active_version", "foo", Type.ADDED, true, AlterNamedRuleItemEvent.class),
                    Arguments.of("UpdateEventWithNamedRuleItemNodePath", "/metadata/fixture/rules/fixture/named/xxx/active_version", "foo", Type.UPDATED, true, AlterNamedRuleItemEvent.class),
                    Arguments.of("DeleteEventWithNamedRuleItemNodePath", "/metadata/fixture/rules/fixture/named/xxx/active_version", "foo", Type.DELETED, true, DropNamedRuleItemEvent.class),
                    Arguments.of("AddEventWithUniqueRuleItemNodePath", "/metadata/fixture/rules/fixture/unique/active_version", "foo", Type.ADDED, true, AlterUniqueRuleItemEvent.class),
                    Arguments.of("UpdateEventWithUniqueRuleItemNodePath", "/metadata/fixture/rules/fixture/unique/active_version", "foo", Type.UPDATED, true, AlterUniqueRuleItemEvent.class),
                    Arguments.of("DeleteEventWithUniqueRuleItemNodePath", "/metadata/fixture/rules/fixture/unique/active_version", "foo", Type.DELETED, true, DropUniqueRuleItemEvent.class));
        }
    }
}
