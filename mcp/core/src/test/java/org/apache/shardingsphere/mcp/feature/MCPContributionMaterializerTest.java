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

package org.apache.shardingsphere.mcp.feature;

import org.apache.shardingsphere.mcp.feature.spi.MCPResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolContribution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class MCPContributionMaterializerTest {
    
    @Test
    void assertMaterializeToolContributionsWithUnsupportedContribution() {
        MCPToolContribution unsupportedContribution = mock(MCPToolContribution.class);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPToolContributionMaterializer.materialize(List.of(unsupportedContribution)));
        assertThat(actual.getMessage(), is(String.format("Unsupported tool contribution `%s`.", unsupportedContribution.getClass().getName())));
    }
    
    @Test
    void assertMaterializeResourceContributionsWithUnsupportedContribution() {
        MCPResourceContribution unsupportedContribution = mock(MCPResourceContribution.class);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPResourceContributionMaterializer.materialize(List.of(unsupportedContribution)));
        assertThat(actual.getMessage(), is(String.format("Unsupported resource contribution `%s`.", unsupportedContribution.getClass().getName())));
    }
}
