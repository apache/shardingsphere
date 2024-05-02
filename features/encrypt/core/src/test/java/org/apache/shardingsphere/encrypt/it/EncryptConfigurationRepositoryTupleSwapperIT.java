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

package org.apache.shardingsphere.encrypt.it;

import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class EncryptConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    EncryptConfigurationRepositoryTupleSwapperIT() {
        super("yaml/encrypt-rule.yaml", new EncryptRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples) {
        assertThat(actualRepositoryTuples.size(), is(3));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertEncryptor(actual.subList(0, 2));
        assertTable(actual.get(2));
    }
    
    private void assertEncryptor(final List<RepositoryTuple> actual) {
        assertThat(actual.get(0).getKey(), is("encryptors/aes_encryptor"));
        assertThat(actual.get(0).getValue(), is("props:\n  aes-key-value: 123456abc\ntype: AES\n"));
        assertThat(actual.get(1).getKey(), is("encryptors/assisted_encryptor"));
        assertThat(actual.get(1).getValue(), is("props:\n  aes-key-value: 123456abc\ntype: AES\n"));
    }
    
    private void assertTable(final RepositoryTuple actual) {
        assertThat(actual.getKey(), is("tables/t_user"));
        assertThat(actual.getValue(), is("columns:\n  username:\n"
                + "    assistedQuery:\n      encryptorName: assisted_encryptor\n      name: assisted_query_username\n    cipher:\n      encryptorName: aes_encryptor\n      name: username_cipher\n"));
    }
}
