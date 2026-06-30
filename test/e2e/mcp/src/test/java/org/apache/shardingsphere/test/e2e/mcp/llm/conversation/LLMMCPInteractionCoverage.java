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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LLMMCPInteractionCoverage {
    
    static boolean hasRequiredInteractionCoverage(final Collection<String> requiredActionNames, final Collection<MCPInteractionTraceRecord> interactionTrace) {
        return findMissingRequiredInteractionNames(requiredActionNames, interactionTrace).isEmpty();
    }
    
    static List<String> findMissingRequiredInteractionNames(final Collection<String> requiredActionNames, final Collection<MCPInteractionTraceRecord> interactionTrace) {
        Set<String> coveredActionNames = getCoveredInteractionNames(interactionTrace);
        return requiredActionNames.stream().filter(each -> !coveredActionNames.contains(each)).toList();
    }
    
    private static Set<String> getCoveredInteractionNames(final Collection<MCPInteractionTraceRecord> interactionTrace) {
        Set<String> result = new LinkedHashSet<>();
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (isSuccessfulInteraction(each)) {
                result.add(each.getTargetName());
            }
        }
        return result;
    }
    
    private static boolean isSuccessfulInteraction(final MCPInteractionTraceRecord interactionTraceRecord) {
        return interactionTraceRecord.isValid() && !interactionTraceRecord.getStructuredContent().containsKey("error_code") && !isHarnessOrigin(interactionTraceRecord.getActionOrigin());
    }
    
    private static boolean isHarnessOrigin(final String actionOrigin) {
        return MCPInteractionTraceRecord.HARNESS_TEXT_RECOVERY_ORIGIN.equals(actionOrigin);
    }
}
