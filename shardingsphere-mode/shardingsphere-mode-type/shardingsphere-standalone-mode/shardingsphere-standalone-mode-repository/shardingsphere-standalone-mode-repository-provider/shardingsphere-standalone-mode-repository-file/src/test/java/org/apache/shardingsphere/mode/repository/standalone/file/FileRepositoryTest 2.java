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
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class FileRepositoryTest {
    
    private FileRepository fileRepository = new FileRepository();
    
    @Test
    public void assertMethod() {
        assertThat(fileRepository.getType(), is("File"));
        assertSetProperty();
        assertPersistAndGet();
        assertPersistAndGetChildrenKeys();
        assertDelete();
    }
    
    private void assertSetProperty() {
        Properties props = new Properties();
        props.setProperty("path", "target");
        fileRepository.setProps(props);
    }
    
    private void assertPersistAndGet() {
        fileRepository.persist(getFilePath(), "test1_content");
        assertThat(fileRepository.get(getFilePath()), is("test1_content" + System.lineSeparator()));
        fileRepository.persist(getFilePath(), "modify_content");
        assertThat(fileRepository.get(getFilePath()), is("modify_content" + System.lineSeparator()));
    }
    
    private void assertPersistAndGetChildrenKeys() {
        assertThat(fileRepository.getChildrenKeys("testDir").get(0), is("test1"));
    }
    
    private void assertDelete() {
        fileRepository.delete("testDir");
        assertFalse((new File(getFilePath())).exists());
    }
    
    private String getFilePath() {
        return Joiner.on(File.separator).join("testDir", "test1");
    }
}
