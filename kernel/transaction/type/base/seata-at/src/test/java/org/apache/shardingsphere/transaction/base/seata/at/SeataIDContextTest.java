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

package org.apache.shardingsphere.transaction.base.seata.at;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeataIDContextTest {
    
    @Test
    void assertIsEmpty() {
        SeataXIDContext.remove();
        assertTrue(SeataXIDContext.isEmpty());
        SeataXIDContext.set("xid");
        assertFalse(SeataXIDContext.isEmpty());
        SeataXIDContext.remove();
    }
    
    @Test
    void assertGet() {
        SeataXIDContext.remove();
        assertNull(SeataXIDContext.get());
        SeataXIDContext.set("xid");
        assertThat(SeataXIDContext.get(), is("xid"));
        SeataXIDContext.remove();
    }
    
    @Test
    void assertSet() {
        SeataXIDContext.remove();
        assertNull(SeataXIDContext.get());
        SeataXIDContext.set("xid");
        assertThat(SeataXIDContext.get(), is("xid"));
        SeataXIDContext.set("xid-2");
        assertThat(SeataXIDContext.get(), is("xid-2"));
        SeataXIDContext.remove();
    }
    
    @Test
    void assertRemove() {
        SeataXIDContext.remove();
        assertNull(SeataXIDContext.get());
        SeataXIDContext.set("xid");
        assertThat(SeataXIDContext.get(), is("xid"));
        SeataXIDContext.remove();
        assertNull(SeataXIDContext.get());
        SeataXIDContext.remove();
    }
}
