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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowIntentResolverSupportTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getResolveOperationTypeCases")
    void assertResolveOperationType(final String name, final WorkflowRequest request, final String expectedOperationType) {
        assertThat(WorkflowIntentResolverSupport.resolveOperationType(request), is(expectedOperationType));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getResolveFieldSemanticsCases")
    void assertResolveFieldSemantics(final String name, final WorkflowRequest request, final String expectedFieldSemantics) {
        assertThat(WorkflowIntentResolverSupport.resolveFieldSemantics(request), is(expectedFieldSemantics));
    }
    
    private static Stream<Arguments> getResolveOperationTypeCases() {
        WorkflowRequest explicitRequest = new WorkflowRequest();
        explicitRequest.setOperationType("alter");
        WorkflowRequest dropRequest = new WorkflowRequest();
        dropRequest.setNaturalLanguageIntent("delete existing rule");
        WorkflowRequest chineseDropRequest = new WorkflowRequest();
        chineseDropRequest.setNaturalLanguageIntent("删除已有规则");
        WorkflowRequest alterRequest = new WorkflowRequest();
        alterRequest.setNaturalLanguageIntent("update one rule configuration");
        WorkflowRequest chineseAlterRequest = new WorkflowRequest();
        chineseAlterRequest.setNaturalLanguageIntent("调整规则配置");
        WorkflowRequest defaultRequest = new WorkflowRequest();
        return Stream.of(
                Arguments.of("explicit operation type", explicitRequest, "alter"),
                Arguments.of("drop intent heuristic", dropRequest, "drop"),
                Arguments.of("chinese drop intent heuristic", chineseDropRequest, "drop"),
                Arguments.of("alter intent heuristic", alterRequest, "alter"),
                Arguments.of("chinese alter intent heuristic", chineseAlterRequest, "alter"),
                Arguments.of("default create", defaultRequest, "create"));
    }
    
    private static Stream<Arguments> getResolveFieldSemanticsCases() {
        WorkflowRequest explicitRequest = new WorkflowRequest();
        explicitRequest.setFieldSemantics("email");
        WorkflowRequest phoneRequest = new WorkflowRequest();
        phoneRequest.setColumn("customer_phone");
        WorkflowRequest chinesePhoneRequest = new WorkflowRequest();
        chinesePhoneRequest.setNaturalLanguageIntent("给手机号加密");
        WorkflowRequest idCardRequest = new WorkflowRequest();
        idCardRequest.setNaturalLanguageIntent("mask the identity card");
        idCardRequest.setColumn("identity");
        WorkflowRequest chineseIdCardRequest = new WorkflowRequest();
        chineseIdCardRequest.setNaturalLanguageIntent("脱敏身份证");
        WorkflowRequest chineseEmailRequest = new WorkflowRequest();
        chineseEmailRequest.setNaturalLanguageIntent("保护邮箱");
        WorkflowRequest defaultRequest = new WorkflowRequest();
        defaultRequest.setColumn("customer_name");
        return Stream.of(
                Arguments.of("explicit field semantics", explicitRequest, "email"),
                Arguments.of("phone heuristic", phoneRequest, "phone"),
                Arguments.of("chinese phone heuristic", chinesePhoneRequest, "phone"),
                Arguments.of("id card heuristic", idCardRequest, "id_card"),
                Arguments.of("chinese id card heuristic", chineseIdCardRequest, "id_card"),
                Arguments.of("chinese email heuristic", chineseEmailRequest, "email"),
                Arguments.of("fallback to column", defaultRequest, "customer_name"));
    }
}
