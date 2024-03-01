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

package org.apache.shardingsphere.infra.util.directory;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathResourceDirectoryReaderTest {
    
    @Test
    void assertIsDirectory() {
        assertTrue(ClasspathResourceDirectoryReader.isDirectory("yaml"));
        assertFalse(ClasspathResourceDirectoryReader.isDirectory("yaml/accepted-class.yaml"));
        assertFalse(ClasspathResourceDirectoryReader.isDirectory("nonexistent"));
    }
    
    @Test
    void read() {
        List<String> resourceNameList = ClasspathResourceDirectoryReader.read("yaml").collect(Collectors.toList());
        assertThat(resourceNameList.size(), is(4));
        final String separator = File.separator;
        assertThat(resourceNameList, hasItems("yaml" + separator + "accepted-class.yaml", "yaml" + separator + "customized-obj.yaml", "yaml" + separator + "empty-config.yaml",
                "yaml" + separator + "shortcuts-fixture.yaml"));
    }
}
