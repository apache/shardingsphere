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

package org.apache.shardingsphere.transaction.yaml.swapper;

import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// TODO Rename YamlTransactionRuleConfigurationSwapperTest when metadata structure adjustment completed. #25485
class NewYamlTransactionRuleConfigurationSwapperTest {
    
    private final NewYamlTransactionRuleConfigurationSwapper swapper = new NewYamlTransactionRuleConfigurationSwapper();
    
    @Test
    void assertSwapToDataNodes() {
        Collection<YamlDataNode> actual = swapper.swapToDataNodes(new TransactionRuleConfiguration("", "", new Properties()));
        assertThat(actual.iterator().next().getKey(), is("transaction"));
    }
}
