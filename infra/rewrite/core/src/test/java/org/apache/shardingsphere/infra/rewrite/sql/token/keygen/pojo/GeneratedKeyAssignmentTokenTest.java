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

package org.apache.shardingsphere.infra.rewrite.sql.token.keygen.pojo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GeneratedKeyAssignmentTokenTest {
    
    private GeneratedKeyAssignmentToken generatedKeyAssignmentToken;
    
    @Test
    void assertCustomGeneratedKeyAssignmentTokenToString() {
        generatedKeyAssignmentToken = new GeneratedKeyAssignmentToken(0, "id") {
            
            @Override
            protected String getRightValue() {
                return "0";
            }
        };
        String resultGeneratedKeyAssignmentTokenToString = generatedKeyAssignmentToken.toString();
        assertThat(resultGeneratedKeyAssignmentTokenToString, is(", id = 0"));
    }
    
    @Test
    void assertLiteralGeneratedKeyAssignmentTokenToString() {
        generatedKeyAssignmentToken = new LiteralGeneratedKeyAssignmentToken(0, "id", "0");
        String resultLiteralGeneratedKeyAssignmentTokenToString = generatedKeyAssignmentToken.toString();
        assertThat(resultLiteralGeneratedKeyAssignmentTokenToString, is(", id = '0'"));
    }
    
    @Test
    void assertLiteralGeneratedKeyAssignmentTokenByIntToString() {
        generatedKeyAssignmentToken = new LiteralGeneratedKeyAssignmentToken(0, "id", 0);
        String resultLiteralGeneratedKeyAssignmentTokenToString = generatedKeyAssignmentToken.toString();
        assertThat(resultLiteralGeneratedKeyAssignmentTokenToString, is(", id = 0"));
    }
    
    @Test
    void assertParameterMarkerGeneratedKeyAssignmentTokenToString() {
        generatedKeyAssignmentToken = new ParameterMarkerGeneratedKeyAssignmentToken(0, "id");
        String resultParameterMarkerGeneratedKeyAssignmentTokenToString = generatedKeyAssignmentToken.toString();
        assertThat(resultParameterMarkerGeneratedKeyAssignmentTokenToString, is(", id = ?"));
    }
}
