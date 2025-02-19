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

package org.apache.shardingsphere.mode.node.path.node.compute;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputeNodePathParser {
    
    private static final String INSTANCE_ID_PATTERN = "([\\S]+)";
    
    /**
     * Find instance id by compute node path.
     *
     * @param computeNodePath compute node path
     * @return found instance ID
     */
    public static Optional<String> findInstanceId(final String computeNodePath) {
        Pattern pattern = Pattern.compile(new ComputeNodePath().getRootPath() + "(/status|/worker_id|/labels)" + "/" + INSTANCE_ID_PATTERN + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(computeNodePath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
}
