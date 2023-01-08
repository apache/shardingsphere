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

public final class LandlineNumberRandomAlgorithmTest {
    
    private LandlineNumberRandomAlgorithm maskAlgorithm;
    
    @Before
    public void setUp() {
        maskAlgorithm = new LandlineNumberRandomAlgorithm();
        maskAlgorithm.init(PropertiesBuilder.build(new Property("landline-numbers", "025, 027, 028, 029, 0310, 0311, 0313")));
    }
    
    @Test
    public void assertMask() {
        assertThat(maskAlgorithm.mask(""), is(""));
        assertThat(maskAlgorithm.mask("0251234567"), not("0251234567"));
        assertThat(maskAlgorithm.mask("03101234567"), not("03101234567"));
    }
}
