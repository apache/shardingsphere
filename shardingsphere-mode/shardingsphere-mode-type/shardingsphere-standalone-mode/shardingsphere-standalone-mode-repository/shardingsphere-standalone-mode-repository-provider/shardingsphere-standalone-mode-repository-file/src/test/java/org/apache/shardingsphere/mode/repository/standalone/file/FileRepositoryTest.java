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

package org.apache.shardingsphere.mode.repository.standalone.file;

import com.google.common.base.Joiner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.File;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class FileRepositoryTest {

    private static final FileRepository FILE_REPOSITORY = new FileRepository();

    private static final String TEST_PATH = Joiner.on(File.separator).join(System.getProperty("user.home"), ".shardingspheretest");

    @BeforeClass
    public static void init() {
        File file = new File(TEST_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    private void assertSetProperty() {
        Properties props = new Properties();
        props.setProperty("path", TEST_PATH);
        FILE_REPOSITORY.setProps(props);
    }

    private void assertPersistAndGet() {
        FILE_REPOSITORY.persist("test1", "test1_content");
        assertThat(FILE_REPOSITORY.get("test1"), is("test1_content" + System.lineSeparator()));
        FILE_REPOSITORY.persist("test1", "modify_content");
        assertThat(FILE_REPOSITORY.get("test1"), is("modify_content" + System.lineSeparator()));
    }

    private void assertPersistAndGetChildrenKeys() {
        FILE_REPOSITORY.persist("testDir/test1", "testDirTest");
        assertThat(FILE_REPOSITORY.getChildrenKeys("testDir").get(0), is("test1"));
        assertThat(FILE_REPOSITORY.get("testDir/test1"), is("testDirTest" + System.lineSeparator()));
    }

    private void assertDelete() {
        FILE_REPOSITORY.delete("test1");
        assertFalse((new File("target/test1")).exists());
        FILE_REPOSITORY.delete("testDir");
        assertFalse((new File("target/testDir")).exists());
    }

    @Test
    public void assertMethod() {
        assertThat(FILE_REPOSITORY.getType(), is("File"));
        assertSetProperty();
        assertPersistAndGet();
        assertPersistAndGetChildrenKeys();
        assertDelete();
    }

    @AfterClass
    public static void clear() {
        File file = new File(TEST_PATH);
        file.delete();
    }
}
