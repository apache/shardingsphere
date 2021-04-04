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

package org.apache.shardingsphere.infra.optimizer.planner.rule;

public final class Rules {
    
    public static final PushJoinToScanRule JOIN_TO_SCAN_RULE = PushJoinToScanRule.Config.DEFAULT.toRule();
    
    public static final PushFilterToScanRule PUSH_FILTER_TO_SCAN_RULE = PushFilterToScanRule.Config.DEFAULT.toRule();
    
    public static final PushProjectToScanRule PUSH_PROJECT_TO_SCAN_RULE = PushProjectToScanRule.Config.DEFAULT.toRule();
    
    public static final PushAggToScanRule PUSH_AGG_TO_SCAN_RULE = PushAggToScanRule.Config.DEFAULT.toRule();
    
    public static final PushSortToScanRule PUSH_SORT_TO_SINGLE_ROUTING_RULE = PushSortToSingleRoutingRule.Config.DEFAULT.toRule();
    
    public static final PushSortToScanRule PUSH_SORT_TO_MULTI_ROUTING_RULE = PushSortToMultiRoutingRule.Config.DEFAULT.toRule();
    
}
