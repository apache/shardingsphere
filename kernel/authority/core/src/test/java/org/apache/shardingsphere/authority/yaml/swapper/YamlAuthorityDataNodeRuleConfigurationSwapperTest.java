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

package org.apache.shardingsphere.authority.yaml.swapper;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class YamlAuthorityDataNodeRuleConfigurationSwapperTest {
    
    private final YamlAuthorityDataNodeRuleConfigurationSwapper swapper = new YamlAuthorityDataNodeRuleConfigurationSwapper();
    
    @Test
    void assertSwapToDataNodes() {
        Collection<ShardingSphereUser> users = Collections.singleton(new ShardingSphereUser("root", "", "localhost"));
        Collection<YamlDataNode> actual = swapper.swapToDataNodes(new AuthorityRuleConfiguration(users, new AlgorithmConfiguration("ALL_PERMITTED", new Properties()),
                Collections.singletonMap("md5", new AlgorithmConfiguration("MD5", createProperties())), "scram_sha256"));
        YamlDataNode yamlDataNode = actual.iterator().next();
        assertThat(yamlDataNode.getKey(), is("authority"));
        assertThat(yamlDataNode.getValue(), containsString("user: root@localhost"));
        assertThat(yamlDataNode.getValue(), containsString("password: ''"));
        assertThat(yamlDataNode.getValue(), containsString("type: ALL_PERMITTED"));
        assertThat(yamlDataNode.getValue(), containsString("defaultAuthenticator: scram_sha256"));
        assertThat(yamlDataNode.getValue(), containsString("type: MD5"));
        assertThat(yamlDataNode.getValue(), containsString("proxy-frontend-database-protocol-type: openGauss"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("proxy-frontend-database-protocol-type", "openGauss");
        return result;
    }
}
