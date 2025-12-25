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

package org.apache.shardingsphere.agent.core.advisor.config;

import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlAdviceFixture;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlTargetObjectFixture;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvisorConfigurationLoaderTest {
    
    private static final String TARGET = YamlTargetObjectFixture.class.getName();
    
    private static final String ADVICE = YamlAdviceFixture.class.getName();
    
    @Test
    void assertLoadAndMergeAdvisorConfigurations() throws IOException {
        File jarFile = File.createTempFile("advisor-config", ".jar");
        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarFile.toPath()))) {
            writeAdvisorResource(jarOutputStream, "fixture", "constructor");
            writeAdvisorResource(jarOutputStream, "another", "method");
        }
        try (JarFile jar = new JarFile(jarFile)) {
            Map<String, AdvisorConfiguration> actual = AdvisorConfigurationLoader.load(Collections.singleton(jar), Arrays.asList("FIXTURE", "ANOTHER", "MISSING"));
            assertThat(actual.size(), is(1));
            AdvisorConfiguration config = actual.get(TARGET);
            assertThat(config.getTargetClassName(), is(TARGET));
            assertThat(config.getAdvisors().size(), is(2));
            assertAdvisor(config.getAdvisors(), ElementMatchers.isConstructor(), "FIXTURE");
            assertAdvisor(config.getAdvisors(), ElementMatchers.named("call"), "ANOTHER");
        } finally {
            assertTrue(jarFile.delete());
        }
    }
    
    private void writeAdvisorResource(final JarOutputStream jarOutputStream, final String pluginType, final String pointcutType) throws IOException {
        JarEntry entry = new JarEntry(String.join("/", "META-INF", "conf", String.join("-", pluginType, "advisors.yaml")));
        jarOutputStream.putNextEntry(entry);
        jarOutputStream.write(createYaml(pointcutType).getBytes());
        jarOutputStream.closeEntry();
    }
    
    private String createYaml(final String pointcutType) {
        return String.join(System.lineSeparator(),
                "advisors:",
                "  - target: " + TARGET,
                "    advice: " + ADVICE,
                "    pointcuts:",
                "      - type: " + pointcutType,
                "        name: call");
    }
    
    private void assertAdvisor(final Collection<MethodAdvisorConfiguration> actual, final ElementMatcher<?> pointcut, final String pluginType) {
        assertTrue(actual.stream().anyMatch(each -> each.getAdviceClassName().equals(ADVICE) && each.getPluginType().equals(pluginType) && each.getPointcut().equals(pointcut)));
    }
}
