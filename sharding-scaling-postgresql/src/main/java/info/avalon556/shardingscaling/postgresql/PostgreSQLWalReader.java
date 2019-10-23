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

package info.avalon556.shardingscaling.postgresql;

import info.avalon566.shardingscaling.core.sync.AbstractRunner;
import info.avalon566.shardingscaling.core.sync.channel.Channel;
import info.avalon566.shardingscaling.core.sync.reader.LogReader;
import lombok.Setter;

/**
 * PostgreSQL WAL reader.
 *
 * @author avalon566
 */
public final class PostgreSQLWalReader extends AbstractRunner implements LogReader {

    @Setter
    private Channel channel;

    @Override
    public void run() {
        //TODO
        read(channel);
    }

    @Override
    public void read(final Channel channel) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void markPosition() {
    }
}

