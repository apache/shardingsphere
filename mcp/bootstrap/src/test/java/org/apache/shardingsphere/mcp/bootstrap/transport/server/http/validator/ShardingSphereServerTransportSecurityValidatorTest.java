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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator;

import io.modelcontextprotocol.server.transport.ServerTransportSecurityException;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.constraint.TransportHeaderConstraint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ShardingSphereServerTransportSecurityValidatorTest {
    
    @Test
    void assertValidateHeadersWithNoFailure() {
        assertDoesNotThrow(() -> new ShardingSphereServerTransportSecurityValidator(mock(), List.of()).validateHeaders(Map.of()));
    }
    
    @Test
    void assertValidateHeadersWithFailure() throws TransportHeaderConstraintException {
        TransportHeaderConstraint first = mock(TransportHeaderConstraint.class);
        doThrow(new TransportHeaderConstraintException(401, "Unauthorized.")).when(first).validate("");
        TransportHeaderConstraint second = mock(TransportHeaderConstraint.class);
        ShardingSphereServerTransportSecurityValidator validator = new ShardingSphereServerTransportSecurityValidator(mock(), List.of(first, second));
        ServerTransportSecurityException actual = assertThrows(ServerTransportSecurityException.class, () -> validator.validateHeaders(Map.of()));
        assertThat(actual.getStatusCode(), is(401));
        assertThat(actual.getMessage(), is("Unauthorized."));
        verifyNoInteractions(second);
    }
}
