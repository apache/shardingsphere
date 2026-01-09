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

package org.apache.shardingsphere.mode.node.rule.tuple;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleField;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class YamlRuleConfigurationReflectionEngineTest {
    
    @Test
    void assertFindClassReturnMatchedClass() {
        try (MockedStatic<ShardingSphereServiceLoader> ignored = mockStatic(ShardingSphereServiceLoader.class)) {
            when(ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)).thenReturn(Collections.singleton(new FixtureYamlRuleConfigurationSwapper()));
            assertThat(YamlRuleConfigurationReflectionEngine.findClass("fixture_rule"), is(FixtureYamlRuleConfiguration.class));
        }
    }
    
    @Test
    void assertFindClassWhenNotFound() {
        try (MockedStatic<ShardingSphereServiceLoader> ignored = mockStatic(ShardingSphereServiceLoader.class)) {
            when(ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)).thenReturn(Collections.singleton(new AnotherYamlRuleConfigurationSwapper()));
            assertThrows(IllegalArgumentException.class, () -> YamlRuleConfigurationReflectionEngine.findClass("missing_rule"));
        }
    }
    
    @Test
    void assertFindClassWhenAnnotationMissing() {
        try (MockedStatic<ShardingSphereServiceLoader> ignored = mockStatic(ShardingSphereServiceLoader.class)) {
            when(ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)).thenReturn(Collections.singleton(new NoAnnotationYamlRuleConfigurationSwapper()));
            assertThrows(IllegalArgumentException.class, () -> YamlRuleConfigurationReflectionEngine.findClass("no_annotation_rule"));
        }
    }
    
    @Test
    void assertGetFieldsOrderedByType() {
        Collection<Field> actual = YamlRuleConfigurationReflectionEngine.getFields(FixtureYamlRuleConfiguration.class);
        assertThat(actual, hasSize(2));
        Iterator<Field> iterator = actual.iterator();
        assertThat(iterator.next().getName(), is("algorithmName"));
        assertThat(iterator.next().getName(), is("strategyName"));
    }
    
    @Test
    void assertGetRuleNodeItemName() throws NoSuchFieldException {
        Field field = FixtureYamlRuleConfiguration.class.getDeclaredField("defaultAlgorithmName");
        assertThat(YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(field), is("default_algorithm_name"));
    }
    
    @Test
    void assertGetRuleNodeItemNameWithSingleCharacter() throws NoSuchFieldException {
        Field field = FixtureYamlRuleConfiguration.class.getDeclaredField("x");
        assertThat(YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(field), is("x"));
    }
    
    @Test
    void assertGetRuleNodeItemNameWithMultipleWords() throws NoSuchFieldException {
        Field field = FixtureYamlRuleConfiguration.class.getDeclaredField("dataSourceName");
        assertThat(YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(field), is("data_source_name"));
    }
    
    @Test
    void assertGetRuleNodeItemNameWithAbbreviations() throws NoSuchFieldException {
        Field field = FixtureYamlRuleConfiguration.class.getDeclaredField("urlDataSource");
        assertThat(YamlRuleConfigurationReflectionEngine.getRuleNodeItemName(field), is("url_data_source"));
    }
    
    @Test
    void assertFindClassWithMultipleSwappers() {
        try (MockedStatic<ShardingSphereServiceLoader> ignored = mockStatic(ShardingSphereServiceLoader.class)) {
            List<YamlRuleConfigurationSwapper> swappers = new ArrayList<>();
            swappers.add(new FixtureYamlRuleConfigurationSwapper());
            swappers.add(new AnotherYamlRuleConfigurationSwapper());
            swappers.add(new NoAnnotationYamlRuleConfigurationSwapper());
            when(ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class)).thenReturn(swappers);
            assertThat(YamlRuleConfigurationReflectionEngine.findClass("fixture_rule"), is(FixtureYamlRuleConfiguration.class));
        }
    }
    
    @Test
    void assertGetFieldsWithNoAnnotatedFields() {
        Collection<Field> actual = YamlRuleConfigurationReflectionEngine.getFields(EmptyYamlRuleConfiguration.class);
        assertThat(actual, empty());
    }
    
    @Test
    void assertGetFieldsWithAllTypeValues() {
        Collection<Field> actual = YamlRuleConfigurationReflectionEngine.getFields(CompleteYamlRuleConfiguration.class);
        assertThat(actual, hasSize(7));
        Iterator<Field> iterator = actual.iterator();
        assertThat(iterator.next().getName(), is("algorithm"));
        assertThat(iterator.next().getName(), is("defaultAlgorithm"));
        assertThat(iterator.next().getName(), is("strategy"));
        assertThat(iterator.next().getName(), is("defaultStrategy"));
        assertThat(iterator.next().getName(), is("dataSource"));
        assertThat(iterator.next().getName(), is("table"));
        assertThat(iterator.next().getName(), is("other"));
    }
    
    @RuleNodeTupleEntity("fixture_rule")
    @Getter
    @Setter
    private static final class FixtureYamlRuleConfiguration implements YamlRuleConfiguration {
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.STRATEGY)
        private String strategyName;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.ALGORITHM)
        private String algorithmName;
        
        private String ignoredField;
        
        private String defaultAlgorithmName;
        
        private String x;
        
        private String dataSourceName;
        
        private String urlDataSource;
        
        @Override
        public Class<? extends RuleConfiguration> getRuleConfigurationType() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    private static final class FixtureRuleConfiguration implements RuleConfiguration {
    }
    
    private static final class EmptyYamlRuleConfiguration implements YamlRuleConfiguration {
        
        private String fieldWithoutAnnotation;
        
        @Override
        public Class<? extends RuleConfiguration> getRuleConfigurationType() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    @RuleNodeTupleEntity("complete_rule")
    @Getter
    @Setter
    private static final class CompleteYamlRuleConfiguration implements YamlRuleConfiguration {
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.OTHER)
        private String other;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.ALGORITHM)
        private String algorithm;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.DEFAULT_ALGORITHM)
        private String defaultAlgorithm;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.STRATEGY)
        private String strategy;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.DEFAULT_STRATEGY)
        private String defaultStrategy;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.DATA_SOURCE)
        private String dataSource;
        
        @RuleNodeTupleField(type = RuleNodeTupleField.Type.TABLE)
        private String table;
        
        @Override
        public Class<? extends RuleConfiguration> getRuleConfigurationType() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    private static final class FixtureYamlRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<FixtureYamlRuleConfiguration, FixtureRuleConfiguration> {
        
        @Override
        public FixtureYamlRuleConfiguration swapToYamlConfiguration(final FixtureRuleConfiguration data) {
            return new FixtureYamlRuleConfiguration();
        }
        
        @Override
        public FixtureRuleConfiguration swapToObject(final FixtureYamlRuleConfiguration yamlConfig) {
            return new FixtureRuleConfiguration();
        }
        
        @Override
        public String getRuleTagName() {
            return "FIXTURE";
        }
        
        @Override
        public int getOrder() {
            return 0;
        }
        
        @Override
        public Class<FixtureRuleConfiguration> getTypeClass() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    private static final class NoAnnotationYamlRuleConfiguration implements YamlRuleConfiguration {
        
        @Override
        public Class<? extends RuleConfiguration> getRuleConfigurationType() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    private static final class NoAnnotationYamlRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<NoAnnotationYamlRuleConfiguration, FixtureRuleConfiguration> {
        
        @Override
        public NoAnnotationYamlRuleConfiguration swapToYamlConfiguration(final FixtureRuleConfiguration data) {
            return new NoAnnotationYamlRuleConfiguration();
        }
        
        @Override
        public FixtureRuleConfiguration swapToObject(final NoAnnotationYamlRuleConfiguration yamlConfig) {
            return new FixtureRuleConfiguration();
        }
        
        @Override
        public String getRuleTagName() {
            return "NO_ANNOTATION";
        }
        
        @Override
        public int getOrder() {
            return 2;
        }
        
        @Override
        public Class<FixtureRuleConfiguration> getTypeClass() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    @RuleNodeTupleEntity("another_rule")
    private static final class AnotherYamlRuleConfiguration implements YamlRuleConfiguration {
        
        @Override
        public Class<? extends RuleConfiguration> getRuleConfigurationType() {
            return FixtureRuleConfiguration.class;
        }
    }
    
    private static final class AnotherYamlRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<AnotherYamlRuleConfiguration, FixtureRuleConfiguration> {
        
        @Override
        public AnotherYamlRuleConfiguration swapToYamlConfiguration(final FixtureRuleConfiguration data) {
            return new AnotherYamlRuleConfiguration();
        }
        
        @Override
        public FixtureRuleConfiguration swapToObject(final AnotherYamlRuleConfiguration yamlConfig) {
            return new FixtureRuleConfiguration();
        }
        
        @Override
        public String getRuleTagName() {
            return "ANOTHER";
        }
        
        @Override
        public int getOrder() {
            return 1;
        }
        
        @Override
        public Class<FixtureRuleConfiguration> getTypeClass() {
            return FixtureRuleConfiguration.class;
        }
    }
}
