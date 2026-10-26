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
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSink;
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSinkCreationContext;
import org.apache.shardingsphere.data.pipeline.cdc.sink.spi.CDCSinkFactory;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CDCSocketSinkFactoryTest {
    
    private final CDCSinkFactory sinkFactory = TypedSPILoader.getService(CDCSinkFactory.class, "SOCKET");
    
    @Test
    void assertCreate() {
        Channel channel = mock(Channel.class);
        CDCSink actual = sinkFactory.create(new CDCSocketSinkCreationContext(channel, mock(ShardingSphereDatabase.class), Collections.emptyList(), new Properties()));
        assertThat(actual, isA(PipelineCDCSocketSink.class));
        assertThat(((PipelineCDCSocketSink) actual).getChannel(), sameInstance(channel));
    }
    
    @Test
    void assertCreateWithIndependentSessions() {
        Channel firstChannel = mock(Channel.class);
        Channel secondChannel = mock(Channel.class);
        CDCSink actualFirst = sinkFactory.create(new CDCSocketSinkCreationContext(firstChannel, mock(), Collections.singletonList("foo_schema.foo_tbl"), new Properties()));
        CDCSink actualSecond = sinkFactory.create(new CDCSocketSinkCreationContext(secondChannel, mock(), Collections.singletonList("bar_schema.bar_tbl"), new Properties()));
        assertThat(actualFirst, not(sameInstance(actualSecond)));
        assertThat(((PipelineCDCSocketSink) actualFirst).getChannel(), sameInstance(firstChannel));
        assertThat(((PipelineCDCSocketSink) actualSecond).getChannel(), sameInstance(secondChannel));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidContextCases")
    void assertCreateWithInvalidContext(final String name, final CDCSinkCreationContext context, final String expectedMessage) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> sinkFactory.create(context));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Stream<Arguments> invalidContextCases() {
        return Stream.of(
                Arguments.of("missing context", null, "Socket sink requires a socket session creation context."),
                Arguments.of("wrong context", mock(CDCSinkCreationContext.class), "Socket sink requires a socket session creation context."),
                Arguments.of("missing channel", new CDCSocketSinkCreationContext(null, mock(), Collections.emptyList(), new Properties()), "Socket sink requires a channel."));
    }
}
