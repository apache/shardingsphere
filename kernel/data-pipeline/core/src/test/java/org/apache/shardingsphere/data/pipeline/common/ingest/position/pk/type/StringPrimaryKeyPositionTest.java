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

package org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type;

import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.PrimaryKeyPositionFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class StringPrimaryKeyPositionTest {
    
    @Test
    void assertInit() {
        StringPrimaryKeyPosition position = (StringPrimaryKeyPosition) PrimaryKeyPositionFactory.newInstance("s,hi,jk");
        assertThat(position.getBeginValue(), is("hi"));
        assertThat(position.getEndValue(), is("jk"));
    }
    
    @Test
    void assertToString() {
        assertThat(new StringPrimaryKeyPosition("hi", "jk").toString(), is("s,hi,jk"));
    }
}
