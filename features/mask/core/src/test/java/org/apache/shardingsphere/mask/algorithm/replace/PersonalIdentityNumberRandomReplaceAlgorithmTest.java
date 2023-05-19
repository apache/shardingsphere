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

package org.apache.shardingsphere.mask.algorithm.replace;

import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class PersonalIdentityNumberRandomReplaceAlgorithmTest {
    
    private PersonalIdentityNumberRandomReplaceAlgorithm maskAlgorithm;
    
    @BeforeEach
    void setUp() {
        maskAlgorithm = new PersonalIdentityNumberRandomReplaceAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("alpha-two-country-area-code", "CN")));
    }
    
    @Test
    void assertMask() {
        assertThat(maskAlgorithm.mask("372928198312103215"), not("372928198312103215"));
        assertThat(maskAlgorithm.mask("372928231210321"), not("372928231210321"));
        assertThat(maskAlgorithm.mask("1234567891011121314"), is("1234567891011121314"));
        assertThat(maskAlgorithm.mask("123456"), is("123456"));
        assertThat(maskAlgorithm.mask(""), is(""));
        assertThat(maskAlgorithm.mask(null), is(nullValue()));
    }
    
    @Test
    void assertMaskWithDifferentCountryCode() {
        PersonalIdentityNumberRandomReplaceAlgorithm maskAlgorithmCN = new PersonalIdentityNumberRandomReplaceAlgorithm();
        maskAlgorithmCN.init(PropertiesBuilder.build(new Property("alpha-two-country-area-code", "CN")));
        PersonalIdentityNumberRandomReplaceAlgorithm maskAlgorithmJP = new PersonalIdentityNumberRandomReplaceAlgorithm();
        maskAlgorithmJP.init(PropertiesBuilder.build(new Property("alpha-two-country-area-code", "JP")));
        assertThat(maskAlgorithmCN.mask("372928198312103215"), not(maskAlgorithmJP.mask("372928198312103215")));
    }
}
