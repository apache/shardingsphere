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

package org.apache.shardingsphere.infra.database.type;

import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DatabaseTypeRegistryTest {
    
    @Test
    public void assertGetTrunkDatabaseTypeNameWithTrunkDatabaseType() {
        assertThat(DatabaseTypeRegistry.getTrunkDatabaseTypeName(new MySQLDatabaseType()), is("MySQL"));
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeNameWithBranchDatabaseType() {
        assertThat(DatabaseTypeRegistry.getTrunkDatabaseTypeName(new MariaDBDatabaseType()), is("MySQL"));
    }
    
    @Test
    public void assertGetActualDatabaseType() {
        assertThat(DatabaseTypeRegistry.getActualDatabaseType("MySQL").getType(), is("MySQL"));
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertGetActualDatabaseTypeWithNotExistedDatabaseType() {
        DatabaseTypeRegistry.getActualDatabaseType("Invalid");
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeWithTrunkDatabaseType() {
        assertThat(DatabaseTypeRegistry.getTrunkDatabaseType("MySQL").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeWithBranchDatabaseType() {
        assertThat(DatabaseTypeRegistry.getTrunkDatabaseType("H2").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDefaultDatabaseType() {
        assertThat(DatabaseTypeRegistry.getDefaultDatabaseType().getType(), is("MySQL"));
    }
}
