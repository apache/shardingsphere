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

package io.shardingsphere.orchestration.reg.newzk.client.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Path hierarchy resolve.
 *
 * @author lidongbo
 */
@RequiredArgsConstructor
final class PathResolve {
    
    @Getter
    private final String path;
    
    @Getter
    private String current;
    
    private int position;
    
    private boolean ended;
    
    private long length = -1;
    
    /**
     * Read position Whether the end position or not .
     *
     * @return isEnd boolean
     */
    public boolean isEnd() {
        return ended;
    }
    
    private void checkEnd() {
        if (length == -1) {
            if (path.charAt(path.length() - 1) == '/') {
                length = path.length() - 1;
            } else {
                length = path.length();
            }
        }
        
        ended = position >= length;
    }
    
    /**
    * Next path node.
    */
    public void next() {
        if (isEnd()) {
            return;
        }
        int nodeBegin = ++position;
        while (!isEnd() && path.charAt(position) != '/') {
            position++;
            checkEnd();
        }
        current = path.substring(nodeBegin, position);
    }
}
