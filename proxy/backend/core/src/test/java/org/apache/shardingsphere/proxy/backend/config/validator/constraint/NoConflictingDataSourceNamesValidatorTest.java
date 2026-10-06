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

package org.apache.shardingsphere.proxy.backend.config.validator.constraint;

import org.apache.bval.jsr.ApacheValidationProvider;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoadResult;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class NoConflictingDataSourceNamesValidatorTest {
    
    private static final Validator VALIDATOR = Validation.byProvider(ApacheValidationProvider.class).configure().buildValidatorFactory().getValidator();
    
    private final NoConflictingDataSourceNamesValidator validator = new NoConflictingDataSourceNamesValidator();
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validConfigsArguments")
    void assertIsValidWithoutConflicts(final String name, final ProxyConfigurationLoadResult config) {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        assertTrue(validator.isValid(config, context));
        verifyNoInteractions(context);
    }
    
    private static Stream<Arguments> validConfigsArguments() {
        Map<String, Collection<String>> sharedLocalNames = new LinkedHashMap<>(2, 1F);
        sharedLocalNames.put("foo_db", Collections.singleton("foo_ds"));
        sharedLocalNames.put("bar_db", Collections.singleton("foo_ds"));
        return Stream.of(
                Arguments.of("No databases", createConfig(Collections.singleton("foo_ds"), Collections.emptyMap())),
                Arguments.of("No global sources", createConfig(Collections.emptyList(), Collections.singletonMap("foo_db", Collections.singleton("foo_ds")))),
                Arguments.of("No database sources", createConfig(Collections.singleton("foo_ds"), Collections.singletonMap("foo_db", Collections.emptyList()))),
                Arguments.of("Distinct names", createConfig(Collections.singleton("foo_ds"), Collections.singletonMap("foo_db", Collections.singleton("bar_ds")))),
                Arguments.of("Case-sensitive names", createConfig(Collections.singleton("foo_ds"), Collections.singletonMap("foo_db", Collections.singleton("FOO_DS")))),
                Arguments.of("Same local names across databases", createConfig(Collections.singleton("global_ds"), sharedLocalNames)));
    }
    
    private static ProxyConfigurationLoadResult createConfig(final Collection<String> globalDataSourceNames, final Map<String, Collection<String>> databaseDataSourceNames) {
        YamlProxyServerConfiguration serverConfig = new YamlProxyServerConfiguration();
        serverConfig.setDataSources(createDataSources(globalDataSourceNames));
        Map<String, YamlProxyDatabaseConfiguration> databaseConfigs = new LinkedHashMap<>(databaseDataSourceNames.size(), 1F);
        for (Entry<String, Collection<String>> entry : databaseDataSourceNames.entrySet()) {
            YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
            databaseConfig.setDatabaseName(entry.getKey());
            databaseConfig.setDataSources(createDataSources(entry.getValue()));
            databaseConfigs.put(entry.getKey(), databaseConfig);
        }
        return new ProxyConfigurationLoadResult(serverConfig, databaseConfigs);
    }
    
    private static Map<String, YamlProxyDataSourceConfiguration> createDataSources(final Collection<String> dataSourceNames) {
        Map<String, YamlProxyDataSourceConfiguration> result = new HashMap<>(dataSourceNames.size(), 1F);
        for (String each : dataSourceNames) {
            result.put(each, new YamlProxyDataSourceConfiguration());
        }
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("conflictingConfigsArguments")
    void assertIsValidWithConflicts(final String name, final ProxyConfigurationLoadResult config, final String expectedDatabaseKey, final Collection<String> expectedDataSourceNames) {
        Collection<ConstraintViolation<ProxyConfigurationLoadResult>> actual = VALIDATOR.validate(config);
        assertThat(actual.size(), is(expectedDataSourceNames.size()));
        Collection<String> actualDataSourceNames = new ArrayList<>(actual.size());
        for (ConstraintViolation<ProxyConfigurationLoadResult> each : actual) {
            List<Path.Node> actualPath = StreamSupport.stream(each.getPropertyPath().spliterator(), false).collect(Collectors.toList());
            assertThat(actualPath.size(), is(3));
            assertThat(actualPath.get(0).getName(), is("databaseConfigurations"));
            assertThat(actualPath.get(1).getName(), is("dataSources"));
            assertThat(actualPath.get(1).getKey(), is(expectedDatabaseKey));
            assertThat(actualPath.get(2).getKind(), is(ElementKind.BEAN));
            actualDataSourceNames.add((String) actualPath.get(2).getKey());
            assertThat(each.getMessage(), is("must not contain data source names shared by global and database configurations"));
        }
        assertThat(actualDataSourceNames, containsInAnyOrder(expectedDataSourceNames.toArray(new String[0])));
    }
    
    private static Stream<Arguments> conflictingConfigsArguments() {
        Map<String, Collection<String>> multipleDatabases = new LinkedHashMap<>(3, 1F);
        multipleDatabases.put("foo_db", Collections.singleton("other_ds"));
        multipleDatabases.put("bar_db", Arrays.asList("foo_ds", "bar_ds"));
        multipleDatabases.put("baz_db", Collections.singleton("baz_ds"));
        String specialDataSourceName = "${validatedValue}[foo].{bar}";
        return Stream.of(
                Arguments.of("One conflicting source", createConfig(Collections.singleton("foo_ds"), Collections.singletonMap("foo_db", Collections.singleton("foo_ds"))),
                        "foo_db", Collections.singleton("foo_ds")),
                Arguments.of("First conflicting database and all its sources", createConfig(Arrays.asList("foo_ds", "bar_ds", "baz_ds"), multipleDatabases), "bar_db",
                        Arrays.asList("foo_ds", "bar_ds")),
                Arguments.of("Special source key", createConfig(Collections.singleton(specialDataSourceName), Collections.singletonMap("foo_db", Collections.singleton(specialDataSourceName))),
                        "foo_db", Collections.singleton(specialDataSourceName)));
    }
}
