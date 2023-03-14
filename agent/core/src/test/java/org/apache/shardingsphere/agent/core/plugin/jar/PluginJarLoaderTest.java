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

package org.apache.shardingsphere.agent.core.plugin.jar;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Objects;
import java.util.jar.JarFile;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PluginJarLoaderTest {
    
    @Test
    public void assertLoad() throws IOException {
        Collection<JarFile> jarFiles = PluginJarLoader.load(new File(getResourceURL()));
        assertThat(jarFiles.size(), is(1));
        assertThat(jarFiles.iterator().next().getName(), endsWith("test-plugin.jar"));
    }
    
    private String getResourceURL() throws UnsupportedEncodingException {
        return URLDecoder.decode(
                Objects.requireNonNull(PluginJarLoader.class.getClassLoader().getResource(""))
                        .getFile(),
                "UTF8");
    }
    
}
