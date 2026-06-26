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

package org.apache.shardingsphere.database.protocol.firebird.err;

import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector.Segment;
import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class FirebirdErrorSegmentResolverTest {
    
    @Test
    void assertResolveRebuildsDuplicateKeyVectorAsSegments() {
        String message = "violation of PRIMARY or UNIQUE KEY constraint \"INTEG_2\" on table \"MY_TABLE\";"
                + " Problematic key value is (\"COL1\" = 1)";
        List<Segment> segments = FirebirdErrorSegmentResolver.resolve(ISCConstants.isc_unique_key_violation, message, "23000");
        assertThat(segments.size(), is(2));
        assertThat(segments.get(0).getGdsCode(), is(ISCConstants.isc_unique_key_violation));
        assertThat(segments.get(0).getArguments(), contains("INTEG_2", "MY_TABLE"));
        assertNull(segments.get(0).getSqlState());
        assertThat(segments.get(1).getGdsCode(), is(ISCConstants.isc_random));
        assertThat(segments.get(1).getArguments(), contains("Problematic key value is (\"COL1\" = 1)"));
    }
    
    @Test
    void assertResolveRendersDuplicateKeyTableNameWithoutNull() {
        String message = "violation of PRIMARY or UNIQUE KEY constraint \"INTEG_2\" on table \"MY_TABLE\";"
                + " Problematic key value is (\"COL1\" = 1)";
        List<Segment> segments = FirebirdErrorSegmentResolver.resolve(ISCConstants.isc_unique_key_violation, message, "23000");
        StringBuilder rendered = new StringBuilder();
        for (Segment each : segments) {
            GDSExceptionHelper.GDSMessage segmentMessage = GDSExceptionHelper.getMessage(each.getGdsCode());
            segmentMessage.setParameters(each.getArguments());
            if (rendered.length() > 0) {
                rendered.append("; ");
            }
            rendered.append(segmentMessage);
        }
        assertThat(rendered.toString(), is(message));
    }
    
    @Test
    void assertResolveUsesRandomCodeWhenErrorCodeIsLowerThanArithExcept() {
        List<Segment> segments = FirebirdErrorSegmentResolver.resolve(ISCConstants.isc_arith_except - 1, "plain", "00000");
        assertThat(segments.size(), is(1));
        assertThat(segments.get(0).getGdsCode(), is(ISCConstants.isc_random));
        assertThat(segments.get(0).getArguments(), is(Arrays.asList("plain")));
    }
    
    @Test
    void assertResolveFailSafeUsesRandomCodeAndKeepsSqlStateForUnmatchedParameterizedTemplate() {
        List<Segment> segments = FirebirdErrorSegmentResolver.resolve(ISCConstants.isc_unique_key_violation, "totally unrelated text", "23000");
        assertThat(segments.size(), is(1));
        assertThat(segments.get(0).getGdsCode(), is(ISCConstants.isc_random));
        assertThat(segments.get(0).getArguments(), contains("totally unrelated text"));
        assertThat(segments.get(0).getSqlState(), is("23000"));
    }
}
