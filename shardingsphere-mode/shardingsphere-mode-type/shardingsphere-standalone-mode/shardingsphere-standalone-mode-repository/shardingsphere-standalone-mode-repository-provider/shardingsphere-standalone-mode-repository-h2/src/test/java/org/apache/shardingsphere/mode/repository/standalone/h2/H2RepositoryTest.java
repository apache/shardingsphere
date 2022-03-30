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

package org.apache.shardingsphere.mode.repository.standalone.h2;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class H2RepositoryTest {
    
    private H2Repository h2Repository = new H2Repository();
    
    @Test
    public void assertMethod() {
        assertThat(h2Repository.getType(), is("H2"));
        assertSetProperty();
        assertPersistAndGet();
        assertPersistAndGetChildrenKeys();
        assertDelete();
        h2Repository.close();
    }
    
    private void assertSetProperty() {
        Properties props = new Properties();
        props.setProperty("jdbc_url", "jdbc:h2:~/h2_repository");
        props.setProperty("user", "sa");
        props.setProperty("password", "");
        props.setProperty("driver_class", "org.h2.Driver");
        h2Repository.setProps(props);
    }
    
    private void assertPersistAndGet() {
        h2Repository.persist("/testPath/test1", "test1_content");
        assertThat(h2Repository.get("/testPath/test1"), is("test1_content"));
        h2Repository.persist("/testPath/test1", "modify_content");
        assertThat(h2Repository.get("/testPath/test1"), is("modify_content"));
    }
    
    private void assertPersistAndGetChildrenKeys() {
        h2Repository.persist("/testPath/test1", "test1_content");
        h2Repository.persist("/testPath/test2", "test2_content");
        List<String> childrenKeys = h2Repository.getChildrenKeys("/testPath");
        assertThat(childrenKeys.get(0), is("test1"));
        assertThat(childrenKeys.get(1), is("test2"));
    }
    
    private void assertDelete() {
        h2Repository.delete("/testPath");
        assertTrue(StringUtils.isBlank(h2Repository.get("/testPath")));
    }
}
