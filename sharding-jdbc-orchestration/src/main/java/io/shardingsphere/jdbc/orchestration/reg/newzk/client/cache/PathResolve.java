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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Path hierarchy resolve.
 *
 * @author lidongbo
 */
@Slf4j
@RequiredArgsConstructor
class PathResolve {
    
    @Getter
    private final List<String> nodes = new ArrayList<>();
    
    @Getter
    private final String path;
    
    @Getter
    private String current;
    
    private int position;
    
    /**
     * Read position Whether the end position or not .
     *
     * @return isEnd boolean
     */
    public boolean isEnd() {
        return position == path.length() - 1;
    }
    
    /**
    * Next path node.
    */
    public void next() {
        if (isEnd()) {
            current = null;
            return;
        }
        int nodeBegin = ++position;
        while (path.charAt(position) != '/') {
            if (isEnd()) {
                position = path.length();
                break;
            }
            position++;
        }
        current = path.substring(nodeBegin, position);
        nodes.add(current);
    }
}
