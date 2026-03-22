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

package org.apache.shardingsphere.mcp.capability;

import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.DatabaseCapability;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.StatementClass;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.SupportedObjectType;
import org.apache.shardingsphere.mcp.capability.DatabaseCapabilityRegistry.TransactionCapability;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseCapabilityRegistryTest {
    
    @Test
    void assertRegister() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        
        registry.register(createCapability());
        
        assertThat(registry.getRegisteredCapabilities().size(), is(1));
        assertTrue(registry.find("MYSQL").isPresent());
    }
    
    @Test
    void assertRegisterWithNull() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        
        NullPointerException actual = assertThrows(NullPointerException.class, () -> registry.register(null));
        
        assertThat(actual.getMessage(), is("capability cannot be null"));
    }
    
    @Test
    void assertFind() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        registry.register(createCapability());
        
        Optional<DatabaseCapability> actual = registry.find(" mysql ");
        
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseType(), is("MYSQL"));
    }
    
    @Test
    void assertFindWithUnknownDatabaseType() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        registry.register(createCapability());
        
        Optional<DatabaseCapability> actual = registry.find("postgresql");
        
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertFindWithNullDatabaseType() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        
        NullPointerException actual = assertThrows(NullPointerException.class, () -> registry.find(null));
        
        assertThat(actual.getMessage(), is("databaseType cannot be null"));
    }
    
    @Test
    void assertGetRegisteredCapabilities() {
        DatabaseCapabilityRegistry registry = new DatabaseCapabilityRegistry();
        registry.register(createCapability());
        
        Collection<DatabaseCapability> actual = registry.getRegisteredCapabilities();
        
        assertThat(actual.size(), is(1));
        assertThrows(UnsupportedOperationException.class, actual::clear);
    }
    
    private DatabaseCapability createCapability() {
        return new DatabaseCapability("mysql", EnumSet.of(SupportedObjectType.TABLE), EnumSet.of(StatementClass.QUERY),
                TransactionCapability.LOCAL_WITH_SAVEPOINT, true, false);
    }
}
