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

package org.apache.shardingsphere.mcp.tool.handler.execute;

import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExecuteSQLToolHandlerTest {
    
    @Test
    void assertGetToolDescriptor() {
        MCPToolDescriptor actual = new ExecuteSQLToolHandler().getToolDescriptor();
        MCPToolFieldDefinition actualSchemaField = actual.getFields().get(1);
        assertThat(actual.getName(), is("execute_query"));
        assertThat(actualSchemaField.getName(), is("schema"));
        assertFalse(actualSchemaField.isRequired());
        assertThat(actualSchemaField.getValueDefinition().getDescription(), is("Optional schema name used as a namespace hint for unqualified object names."));
    }
}
