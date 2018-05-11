/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.routing.type;

import lombok.Getter;

/**
 * Routing result.
 * 
 * @author zhangliang
 */
@Getter
public class RoutingResult {
    
    private final TableUnits tableUnits = new TableUnits();
    
    /**
     * Adjust is route for single database and table only or not.
     *
     * @return is route for single database and table only or not
     */
    public boolean isSingleRouting() {
        return 1 == tableUnits.getTableUnits().size();
    }
}
