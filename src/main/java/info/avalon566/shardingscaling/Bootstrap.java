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

package info.avalon566.shardingscaling;

import info.avalon566.shardingscaling.job.ScalingJob;
import info.avalon566.shardingscaling.job.schedule.standalone.InProcessScheduler;
import info.avalon566.shardingscaling.utils.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.util.Arrays;

/**
 * Bootstrap of ShardingScaling.
 *
 * @author avalon566
 */
@Slf4j
public class Bootstrap {

    static {
        PropertyConfigurator.configure(RuntimeUtil.getBasePath() + "conf" + File.separator + "log4j.properties");
    }
    
    /**
     * Main entry.
     *
     * @param args running args
     */
    public static void main(final String[] args) {
        log.info("ShardingScaling Startup");
        var scheduler = new InProcessScheduler();
        if ("scaling".equals(args[0])) {
            new ScalingJob(Arrays.copyOfRange(args, 1, args.length), scheduler).run();
        }
    }
}
