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

package org.apache.shardingsphere.proxy.backend.state.impl;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.proxy.backend.exception.UnavailableException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UnavailableProxyStateTest {
    
    @Test
    void assertExecuteWithUnsupportedSQL() {
        assertThrows(UnavailableException.class, () -> new UnavailableProxyState().check(mock(DMLStatement.class)));
    }
    
    @Test
    void assertExecuteWithSupportedSQL() {
        new UnavailableProxyState().check(mock(ImportMetaDataStatement.class));
    }
}
