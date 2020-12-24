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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class FileSystemResumeBreakPointManagerTest {
    
    private FileSystemResumeBreakPointManager resumeBreakPointManager;
    
    @Before
    public void setUp() {
        resumeBreakPointManager = new FileSystemResumeBreakPointManager("H2", "target/.scaling");
    }
    
    @Test
    public void assertPersistAndGetPosition() {
        resumeBreakPointManager.persistPosition();
        assertThat(resumeBreakPointManager.getPosition("target/.scaling/incremental"), is("{}"));
        assertThat(resumeBreakPointManager.getPosition("target/.scaling/inventory"), is("{\"unfinished\":{},\"finished\":[]}"));
    }
    
    @After
    @SneakyThrows(IOException.class)
    public void tearDown() {
        resumeBreakPointManager.close();
        FileUtils.forceDeleteOnExit(new File("target/.scaling"));
    }
}
