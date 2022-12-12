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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class H2DataSourceMetaDataTest {
    
    @Test
    public void assertNewConstructorWithMem() {
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertThat(actual.getHostname(), is(""));
        assertThat(actual.getPort(), is(-1));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertNull(actual.getSchema());
    }
    
    @Test
    public void assertNewConstructorWithSymbol() {
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:~:ds-0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertThat(actual.getHostname(), is(""));
        assertThat(actual.getPort(), is(-1));
        assertNull(actual.getSchema());
    }
    
    @Test
    public void assertNewConstructorWithTcp() {
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1");
        assertThat(actual.getHostname(), is("localhost"));
        assertThat(actual.getPort(), is(8082));
        assertThat(actual.getCatalog(), is("test2"));
        assertNull(actual.getSchema());
    }
    
    @Test
    public void assertNewConstructorWithSsl() {
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:ssl:180.76.76.76/home/test");
        assertThat(actual.getHostname(), is("180.76.76.76"));
        assertThat(actual.getPort(), is(-1));
        assertThat(actual.getCatalog(), is("test"));
        assertNull(actual.getSchema());
    }
    
    @Test
    public void assertNewConstructorWithFile() {
        H2DataSourceMetaData actual = new H2DataSourceMetaData("jdbc:h2:file:/data/sample;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        assertThat(actual.getHostname(), is(""));
        assertThat(actual.getPort(), is(-1));
        assertThat(actual.getCatalog(), is("sample"));
        assertNull(actual.getSchema());
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithMem() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithSymbol() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:~:ds-0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:~:ds-1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstance() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:~:ds-1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithTcp() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:tcp://localhost:8082/~/test3/test4;DB_CLOSE_DELAY=-1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertFalseIsInSameDatabaseInstanceWithTcp() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:tcp://192.168.64.76:8082/~/test3/test4;DB_CLOSE_DELAY=-1");
        assertFalse(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithSsl() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:ssl:180.76.76.76/home/test-one");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:ssl:180.76.76.76/home/test-two");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertFalseIsInSameDatabaseInstanceWithSsl() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:ssl:180.76.76.76/home/test-one");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:ssl:181.76.76.76/home/test-two");
        assertFalse(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithFile() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:file:/data/sample-one;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:file:/data/sample-two;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
}
