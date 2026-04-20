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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptWorkflowOptionsTest {
    
    @Test
    void assertCopyCreatesDetachedOptions() {
        EncryptWorkflowOptions originalOptions = new EncryptWorkflowOptions();
        originalOptions.setRequiresDecrypt(true);
        originalOptions.setCipherColumnName("phone_cipher");
        originalOptions.getAssistedQueryAlgorithmProperties().put("digest-algorithm-name", "SHA-256");
        final EncryptWorkflowOptions actualOptions = originalOptions.copy();
        originalOptions.setRequiresDecrypt(false);
        originalOptions.setCipherColumnName("phone_cipher_v2");
        originalOptions.getAssistedQueryAlgorithmProperties().put("salt", "abc");
        assertTrue(actualOptions.getRequiresDecrypt());
        assertThat(actualOptions.getCipherColumnName(), is("phone_cipher"));
        assertThat(actualOptions.getAssistedQueryAlgorithmProperties().size(), is(1));
    }
    
    @Test
    void assertOverlayKeepsExistingValuesForBlankInputs() {
        EncryptWorkflowOptions previousOptions = new EncryptWorkflowOptions();
        previousOptions.setAllowIndexDDL(false);
        previousOptions.setAssistedQueryAlgorithmType("MD5");
        previousOptions.setCipherColumnName("phone_cipher");
        previousOptions.getAssistedQueryAlgorithmProperties().put("digest-algorithm-name", "SHA-256");
        EncryptWorkflowOptions currentOptions = new EncryptWorkflowOptions();
        currentOptions.setRequiresLikeQuery(true);
        currentOptions.setAssistedQueryAlgorithmType("   ");
        currentOptions.setCipherColumnName(null);
        currentOptions.getLikeQueryAlgorithmProperties().put("delta", "1");
        currentOptions.overlayTo(previousOptions);
        assertFalse(previousOptions.getAllowIndexDDL());
        assertTrue(previousOptions.getRequiresLikeQuery());
        assertThat(previousOptions.getAssistedQueryAlgorithmType(), is("MD5"));
        assertThat(previousOptions.getCipherColumnName(), is("phone_cipher"));
        assertThat(previousOptions.getAssistedQueryAlgorithmProperties().get("digest-algorithm-name"), is("SHA-256"));
        assertThat(previousOptions.getLikeQueryAlgorithmProperties().get("delta"), is("1"));
    }
}
