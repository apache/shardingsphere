/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.internal.jdbc.metadata;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public final class CircuitBreakerDatabaseMetaDataTest {
    
    private CircuitBreakerDatabaseMetaData metaData = new CircuitBreakerDatabaseMetaData();
    
    @Test
    public void assertAllProceduresAreCallable() throws Exception {
        assertFalse(metaData.allProceduresAreCallable());
    }
    
    @Test
    public void assertAllTablesAreSelectable() throws Exception {
        assertFalse(metaData.allTablesAreSelectable());
    }
    
    @Test
    public void assertGetURL() throws Exception {
        assertNull(metaData.getURL());
    }
    
    @Test
    public void assertGetUserName() throws Exception {
        assertNull(metaData.getUserName());
    }
    
    @Test
    public void assertIsReadOnly() throws Exception {
        assertFalse(metaData.isReadOnly());
    }
    
    @Test
    public void assertNullsAreSortedHigh() throws Exception {
        assertFalse(metaData.isReadOnly());
    }
    
}
