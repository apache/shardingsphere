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

package org.apache.shardingsphere.test.natived.jdbc.commons.testcontainers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Capability;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal", "resource", "DataFlowIssue", "unused"})
public class ShardingSphereConsulContainer extends GenericContainer<ShardingSphereConsulContainer> {
    
    private static final DockerImageName DEFAULT_OLD_IMAGE_NAME = DockerImageName.parse("consul");
    
    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("hashicorp/consul");
    
    private static final int CONSUL_HTTP_PORT = 8500;
    
    private static final int CONSUL_GRPC_PORT = 8502;
    
    private List<String> initCommands = new ArrayList<>();
    
    private String[] startConsulCmd = new String[]{"agent", "-dev", "-client", "0.0.0.0"};
    
    /**
     * Manually specify the Port for ShardingSphere's nativeTest.
     * @param dockerImageName docker image name
     */
    public ShardingSphereConsulContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_OLD_IMAGE_NAME, DEFAULT_IMAGE_NAME);
        setWaitStrategy(Wait.forHttp("/v1/status/leader").forPort(CONSUL_HTTP_PORT).forStatusCode(200));
        withCreateContainerCmdModifier(cmd -> {
            cmd.getHostConfig().withCapAdd(Capability.IPC_LOCK);
            cmd.withHostConfig(new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(62391), new ExposedPort(CONSUL_HTTP_PORT))));
        });
        withEnv("CONSUL_ADDR", "http://0.0.0.0:" + CONSUL_HTTP_PORT);
        withCommand(startConsulCmd);
    }
    
    @Override
    protected void containerIsStarted(final InspectContainerResponse containerInfo) {
        if (!initCommands.isEmpty()) {
            String commands = initCommands.stream().map(command -> "consul " + command).collect(Collectors.joining(" && "));
            try {
                ExecResult execResult = this.execInContainer("/bin/sh", "-c", commands);
                if (0 != execResult.getExitCode()) {
                    logger().error(
                            "Failed to execute these init commands {}. Exit code {}. Stdout {}. Stderr {}",
                            initCommands,
                            execResult.getExitCode(),
                            execResult.getStdout(),
                            execResult.getStderr());
                }
            } catch (IOException | InterruptedException e) {
                logger().error(
                        "Failed to execute these init commands {}. Exception message: {}",
                        initCommands,
                        e.getMessage());
            }
        }
    }
    
    /**
     * work with Consul Command.
     * @param commands The commands to send to the consul cli
     * @return this
     */
    public ShardingSphereConsulContainer withConsulCommand(final String... commands) {
        initCommands.addAll(Arrays.asList(commands));
        return self();
    }
}
