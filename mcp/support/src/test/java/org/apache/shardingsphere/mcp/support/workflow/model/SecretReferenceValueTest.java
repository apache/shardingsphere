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

package org.apache.shardingsphere.mcp.support.workflow.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecretReferenceValueTest {
    
    @Test
    void assertCreate() {
        SecretReferenceValue actual = SecretReferenceValue.create();
        assertFalse(actual.isMalformed());
    }
    
    @Test
    void assertMalformed() {
        SecretReferenceValue actual = SecretReferenceValue.malformed();
        assertTrue(actual.isMalformed());
    }
    
    @Test
    void assertCreatePlaceholder() {
        assertThat(SecretReferenceValue.createPlaceholder("primary", "aes-key-value"), is("secret_reference:primary.aes-key-value"));
    }
    
    @Test
    void assertCreateManualPlaceholder() {
        assertThat(SecretReferenceValue.createManualPlaceholder("primary", "aes-key-value"), is("<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"));
    }
    
    @Test
    void assertToSafeSummaryUsesNeutralLabel() {
        Map<String, Object> actual = SecretReferenceValue.create().toSafeSummary("primary", "aes-key-value");
        assertThat(actual.get("algorithm_role"), is("primary"));
        assertThat(actual.get("property_key"), is("aes-key-value"));
        assertThat(actual.get("label"), is("secret_placeholder:primary.aes-key-value"));
        assertThat(actual.get("manual_placeholder"), is("<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"));
        assertTrue((Boolean) actual.get("replacement_required"));
        assertFalse((Boolean) actual.get("malformed"));
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
        assertFalse(String.valueOf(actual).contains("user label"));
    }
}
