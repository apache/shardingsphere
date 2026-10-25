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

package org.apache.shardingsphere.data.pipeline.cdc.sink.socket;

import io.netty.channel.Channel;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.sink.PipelineCDCSocketSink;
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSink;
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSinkFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CDCSocketSinkFactoryIT {
    
    @Test
    void assertCreate() throws IOException, URISyntaxException {
        CDCSinkFactory actualFactory = TypedSPILoader.getService(CDCSinkFactory.class, "SOCKET");
        assertThat(actualFactory, isA(CDCSocketSinkFactory.class));
        try (JarFile actualJar = new JarFile(new File(actualFactory.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            assertNotNull(actualJar.getJarEntry("META-INF/maven/org.apache.shardingsphere/shardingsphere-data-pipeline-cdc-sink-socket/pom.properties"));
            JarEntry serviceEntry = actualJar.getJarEntry("META-INF/services/" + CDCSinkFactory.class.getName());
            assertNotNull(serviceEntry);
            try (BufferedReader service = new BufferedReader(new InputStreamReader(actualJar.getInputStream(serviceEntry), StandardCharsets.UTF_8))) {
                assertThat(service.lines().filter(each -> !each.startsWith("#") && !each.isEmpty()).collect(Collectors.toList()), is(Collections.singletonList(CDCSocketSinkFactory.class.getName())));
            }
        }
        Channel channel = mock(Channel.class);
        try (CDCSink actual = actualFactory.create(new CDCSocketSinkCreationContext(channel, mock(), Collections.singletonList("foo_schema.foo_tbl"), new Properties()))) {
            assertThat(actual, isA(PipelineCDCSocketSink.class));
            assertThat(((PipelineCDCSocketSink) actual).getChannel(), sameInstance(channel));
        }
    }
}
