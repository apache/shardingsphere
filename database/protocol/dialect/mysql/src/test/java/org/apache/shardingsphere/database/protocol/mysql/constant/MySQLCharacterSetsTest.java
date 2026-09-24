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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCharsetException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownCollationException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLCharacterSetsTest {
    
    @Test
    void assertFoundCharacterSetById() {
        MySQLCharacterSets actual = MySQLCharacterSets.findById(45);
        assertThat(actual, is(MySQLCharacterSets.UTF8MB4_GENERAL_CI));
    }
    
    @Test
    void assertCharacterSetNotFoundById() {
        assertThrows(UnknownCollationException.class, () -> MySQLCharacterSets.findById(-1));
    }
    
    @Test
    void assertFoundUnsupportedCharacterSetById() {
        assertThrows(UnknownCollationException.class, () -> MySQLCharacterSets.findById(63));
    }
    
    @Test
    void assertFindByCharacterSetName() {
        assertThat(MySQLCharacterSets.findByCharacterSetName("UTF8MB4"), is(MySQLCharacterSets.UTF8MB4_GENERAL_CI));
    }
    
    @Test
    void assertFindDefaultCollationByCharacterSetName() {
        assertThat(MySQLCharacterSets.findByCharacterSetName("latin1"), is(MySQLCharacterSets.LATIN1_SWEDISH_CI));
    }
    
    @Test
    void assertCharacterSetNotFoundByName() {
        assertThrows(UnknownCharsetException.class, () -> MySQLCharacterSets.findByCharacterSetName("unknown_charset"));
    }
    
    @Test
    void assertFindByCollationName() {
        assertThat(MySQLCharacterSets.findByCollationName("UTF8MB4_BIN"), is(MySQLCharacterSets.UTF8MB4_BIN));
    }
    
    @Test
    void assertCollationNotFoundByName() {
        assertThrows(UnknownCollationException.class, () -> MySQLCharacterSets.findByCollationName("unknown_collation"));
    }
    
    @Test
    void assertGetCharacterSetName() {
        assertThat(MySQLCharacterSets.UTF8MB4_GENERAL_CI.getCharacterSetName(), is("utf8mb4"));
    }
    
    @Test
    void assertGetBinaryCharacterSetName() {
        assertThat(MySQLCharacterSets.BINARY.getCharacterSetName(), is("binary"));
    }
    
    @Test
    void assertGetCollationName() {
        assertThat(MySQLCharacterSets.UTF8MB4_GENERAL_CI.getCollationName(), is("utf8mb4_general_ci"));
    }
}
