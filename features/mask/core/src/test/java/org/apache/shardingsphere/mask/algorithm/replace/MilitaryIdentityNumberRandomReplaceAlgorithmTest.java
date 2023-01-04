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
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public final class MilitaryIdentityNumberRandomReplaceAlgorithmTest {
    
    private MilitaryIdentityNumberRandomReplaceAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new MilitaryIdentityNumberRandomReplaceAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("type-codes", "军,人,士,文,职")));
    }
    
    @Test
    public void assertMask() {
        assertThat(maskAlgorithm.mask("军字第1234567号"), not("军字第1234567号"));
        assertThat(maskAlgorithm.mask("军字第1234567号".substring(0, 1)), not("军"));
        assertThat(maskAlgorithm.mask("军字第1234567号".substring(3, 10)), not("1234567"));
        assertThat(maskAlgorithm.mask(""), is(""));
    }
}
