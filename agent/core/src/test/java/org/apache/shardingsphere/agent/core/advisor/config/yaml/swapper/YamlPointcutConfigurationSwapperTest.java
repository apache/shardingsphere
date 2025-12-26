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

package org.apache.shardingsphere.agent.core.advisor.config.yaml.swapper;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.ForLoadedConstructor;
import net.bytebuddy.description.method.MethodDescription.ForLoadedMethod;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutParameterConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlPointcutConfigurationSwapperTest {
    
    @Test
    void assertSwapConstructorPointcutWithModifiersParametersAndReturn() throws NoSuchMethodException {
        YamlPointcutConfiguration yamlConfig = new YamlPointcutConfiguration();
        yamlConfig.setType("constructor");
        yamlConfig.setModifiers("public invalid static");
        yamlConfig.setParamLength(1);
        yamlConfig.setReturnType(String.class.getName());
        YamlPointcutParameterConfiguration parameterConfig = new YamlPointcutParameterConfiguration();
        parameterConfig.setIndex(0);
        parameterConfig.setType(String.class.getName());
        yamlConfig.getParams().add(parameterConfig);
        Optional<ElementMatcher<MethodDescription>> actual = YamlPointcutConfigurationSwapper.swap(yamlConfig);
        assertTrue(actual.isPresent());
        assertFalse(actual.get().matches(new ForLoadedConstructor(ConstructorFixture.class.getDeclaredConstructor(String.class))));
    }
    
    @Test
    void assertSwapMethodPointcutAndInvalidType() throws NoSuchMethodException {
        YamlPointcutConfiguration methodConfig = new YamlPointcutConfiguration();
        methodConfig.setType("method");
        methodConfig.setName("execute");
        Optional<ElementMatcher<MethodDescription>> methodMatcher = YamlPointcutConfigurationSwapper.swap(methodConfig);
        assertTrue(methodMatcher.isPresent());
        ForLoadedMethod methodDescription = new ForLoadedMethod(MethodFixture.class.getDeclaredMethod("execute"));
        assertTrue(methodMatcher.get().matches(methodDescription));
        YamlPointcutConfiguration unsupportedConfig = new YamlPointcutConfiguration();
        unsupportedConfig.setType("unknown");
        assertFalse(YamlPointcutConfigurationSwapper.swap(unsupportedConfig).isPresent());
    }
    
    @Test
    void assertSwapMethodPointcutWithNegativeParamLength() throws NoSuchMethodException {
        YamlPointcutConfiguration methodConfig = new YamlPointcutConfiguration();
        methodConfig.setType("method");
        methodConfig.setName("execute");
        methodConfig.setParamLength(-1);
        Optional<ElementMatcher<MethodDescription>> methodMatcher = YamlPointcutConfigurationSwapper.swap(methodConfig);
        assertTrue(methodMatcher.isPresent());
        ForLoadedMethod methodDescription = new ForLoadedMethod(MethodFixture.class.getDeclaredMethod("execute"));
        assertTrue(methodMatcher.get().matches(methodDescription));
    }
    
    private static final class ConstructorFixture {
        
        @SuppressWarnings("unused")
        ConstructorFixture() {
        }
        
        @SuppressWarnings("unused")
        ConstructorFixture(final String value) {
        }
    }
    
    private static final class MethodFixture {
        
        @SuppressWarnings("unused")
        private void execute() {
        }
    }
}
