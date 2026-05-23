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
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LLMMCPScenarioInference {
    
    static final String PLANNING_TOOL_NAME_PREFIX = "database_gateway_plan_";
    
    private static final Pattern RESOURCE_URI_PATTERN = Pattern.compile("shardingsphere://[^`\\s,]+");
    
    static String findExpectedResourceUri(final LLME2EScenario scenario) {
        final String expectedTableResourceUri = createExpectedTableResourceUri(scenario.getExpectedAnswer());
        String promptResourceUri = "";
        final Matcher matcher = RESOURCE_URI_PATTERN.matcher(scenario.getUserPrompt());
        while (matcher.find()) {
            final String each = trimResourceUri(matcher.group());
            if (each.equals(expectedTableResourceUri)) {
                return each;
            }
            if (promptResourceUri.isEmpty()) {
                promptResourceUri = each;
            }
        }
        return promptResourceUri.isEmpty() ? expectedTableResourceUri : promptResourceUri;
    }
    
    private static String trimResourceUri(final String resourceUri) {
        return resourceUri.replaceAll("[.)\\]]+$", "");
    }
    
    private static String createExpectedTableResourceUri(final LLMStructuredAnswer expectedAnswer) {
        return expectedAnswer.getDatabase().isEmpty() || expectedAnswer.getSchema().isEmpty() || expectedAnswer.getTable().isEmpty()
                ? ""
                : String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", expectedAnswer.getDatabase(), expectedAnswer.getSchema(), expectedAnswer.getTable());
    }
    
    static String findLatestPlanId(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            final MCPInteractionTraceRecord each = interactionTrace.get(index);
            final String result = Objects.toString(each.getStructuredContent().get("plan_id"), "").trim();
            if (each.isValid() && !result.isEmpty() && !each.getStructuredContent().containsKey("error_code")) {
                return result;
            }
        }
        return "";
    }
    
    static String normalizeComparableQuery(final LLMStructuredAnswer expectedAnswer, final String query) {
        final String result = normalizeComparableQuery(query);
        if (expectedAnswer.getSchema().isEmpty() || expectedAnswer.getTable().isEmpty()) {
            return result;
        }
        return result.replaceAll("\\b" + Pattern.quote(expectedAnswer.getSchema().toUpperCase(Locale.ENGLISH)) + "\\."
                + Pattern.quote(expectedAnswer.getTable().toUpperCase(Locale.ENGLISH)) + "\\b", Matcher.quoteReplacement(expectedAnswer.getTable().toUpperCase(Locale.ENGLISH)));
    }
    
    private static String normalizeComparableQuery(final String query) {
        return query.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ENGLISH);
    }
}
