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

package org.apache.shardingsphere.sql.parser.sql.common.constant;

import java.util.Arrays;

/**
 * Direction type enum.
 */
public enum DirectionType {
    
    NEXT, PRIOR, FIRST, LAST, ABSOLUTE_COUNT, RELATIVE_COUNT, COUNT, ALL, FORWARD, FORWARD_COUNT, FORWARD_ALL, BACKWARD, BACKWARD_COUNT, BACKWARD_ALL;
    
    /**
     * Is direction type.
     * 
     * @param directionType direction type
     * @return is direction type or not
     */
    public static boolean isAggregationType(final String directionType) {
        return Arrays.stream(values()).anyMatch(each -> directionType.equalsIgnoreCase(each.name()));
    }
}
