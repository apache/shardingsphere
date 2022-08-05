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

import org.apache.shardingsphere.mode.repository.standalone.jdbc.JDBCRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class H2JDBCRepositoryTest {
    
    private final JDBCRepository repository = new JDBCRepository();
    
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty("jdbc_url", "jdbc:h2:mem:config;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        props.setProperty("username", "sa");
        props.setProperty("password", "");
        props.setProperty("provider", "H2");
        repository.init(props);
    }
    
    @After
    public void stop() {
        repository.close();
    }
    
    @Test
    public void assertPersistAndGet() {
        repository.persist("/testPath/test1", "test1_content");
        assertThat(repository.get("/testPath/test1"), is("test1_content"));
        repository.persist("/testPath/test1", "modify_content");
        assertThat(repository.get("/testPath/test1"), is("modify_content"));
    }
    
    @Test
    public void assertPersistAndGetChildrenKeys() {
        repository.persist("/testPath/test1", "test1_content");
        repository.persist("/testPath/test2", "test2_content");
        List<String> childrenKeys = repository.getChildrenKeys("/testPath");
        assertThat(childrenKeys.get(0), is("test1"));
        assertThat(childrenKeys.get(1), is("test2"));
    }
    
    @Test
    public void assertDelete() {
        repository.delete("/testPath");
        assertThat(repository.get("/testPath"), is(""));
    }
}
