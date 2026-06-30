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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Readwrite-splitting load-balance algorithm property template service.
 */
public final class ReadwriteSplittingAlgorithmPropertyTemplateService {
    
    /**
     * Find load-balance property requirements.
     *
     * @param algorithmType load-balance algorithm type
     * @param readStorageUnits read storage units
     * @return property requirements
     */
    public List<AlgorithmPropertyRequirement> findRequirements(final String algorithmType, final Collection<String> readStorageUnits) {
        List<AlgorithmPropertyRequirement> result = new LinkedList<>();
        if (!"WEIGHT".equals(Objects.toString(algorithmType, "").trim().toUpperCase(Locale.ENGLISH))) {
            return result;
        }
        for (String each : readStorageUnits) {
            if (!Objects.toString(each, "").trim().isEmpty()) {
                result.add(new AlgorithmPropertyRequirement("primary", each, true, false,
                        "Relative read traffic weight for this read storage unit.", ""));
            }
        }
        return result;
    }
}
