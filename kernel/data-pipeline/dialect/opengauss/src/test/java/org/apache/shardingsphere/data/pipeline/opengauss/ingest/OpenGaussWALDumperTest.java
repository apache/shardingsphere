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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest;

import org.apache.shardingsphere.infra.util.reflection.ReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class OpenGaussWALDumperTest {
    
    @Test
    void assertGetVersion() throws NoSuchMethodException {
        OpenGaussWALDumper dumper = mock(OpenGaussWALDumper.class);
        int version = ReflectionUtils.invokeMethod(OpenGaussWALDumper.class.getDeclaredMethod("parseMajorVersion", String.class), dumper,
                "(openGauss 3.1.0 build ) compiled at 2023-02-17 16:13:51 commit 0 last mr   on x86_64-unknown-linux-gnu, compiled by g++ (GCC) 7.3.0, 64-bit");
        assertThat(version, is(3));
        OpenGaussWALDumper mock = mock(OpenGaussWALDumper.class);
        version = ReflectionUtils.invokeMethod(OpenGaussWALDumper.class.getDeclaredMethod("parseMajorVersion", String.class), mock, "(openGauss 5.0.1 build )");
        assertThat(version, is(5));
        version = ReflectionUtils.invokeMethod(OpenGaussWALDumper.class.getDeclaredMethod("parseMajorVersion", String.class), mock, "not match");
        assertThat(version, is(2));
    }
}
