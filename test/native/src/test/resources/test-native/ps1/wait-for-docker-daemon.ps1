#Requires -Version 7.4

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# This file is only used in the PowerShell 7 of ShardingSphere in GitHub Actions environment and should not be executed manually in a development environment.
# Background information can be found at https://github.com/apache/shardingsphere/pull/35905 .
param (
    [int] $MaxAttempts = 2,
    [int] $TimeoutMinutes = 5
)

function Invoke-DockerCommand
{
    param (
        [string[]] $Arguments
    )
    docker @Arguments
    if (0 -ne $LASTEXITCODE)
    {
        throw "docker $($Arguments -join ' ') failed with exit code $LASTEXITCODE."
    }
}

function Wait-DockerDaemon
{
    $deadline = (Get-Date).AddMinutes($TimeoutMinutes)
    $lastError = "UNKNOWN"
    while ((Get-Date) -lt $deadline)
    {
        $now = Get-Date
        $deadlineString = $deadline.ToString("u")
        Write-Host "Waiting for Docker daemon: ($lastError) $now / $deadlineString"
        try
        {
            Invoke-DockerCommand @("version", "--format", "{{.Server.Version}}")
            Invoke-DockerCommand @("info", "--format", "{{.ServerVersion}}")
            Write-Host "Docker daemon is ready."
            return $true
        }
        catch
        {
            $lastError = $_.Exception.Message
            Write-Host "Docker daemon is not ready: $lastError"
        }
        Start-Sleep -Seconds 10
    }
    Write-Host "Timed out waiting for Docker daemon."
    Write-Host "Current time: $( Get-Date )"
    Write-Host "Deadline: $($deadline.ToString("u") )"
    Write-Host "Last error: $lastError"
    return $false
}

function Restart-RancherDesktop
{
    Write-Host "Restarting Rancher Desktop before retrying Docker daemon readiness."
    rdctl shutdown
    if (0 -ne $LASTEXITCODE)
    {
        Write-Host "rdctl shutdown failed with exit code $LASTEXITCODE, continuing with start."
    }
    rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
    if (0 -ne $LASTEXITCODE)
    {
        throw "rdctl start failed with exit code $LASTEXITCODE."
    }
    ./test/native/src/test/resources/test-native/ps1/wait-for-rancher-desktop-backend.ps1
}

for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++)
{
    Write-Host "Docker daemon readiness attempt $attempt of $MaxAttempts."
    if (Wait-DockerDaemon)
    {
        exit 0
    }
    if ($attempt -lt $MaxAttempts)
    {
        Restart-RancherDesktop
    }
}
Write-Error "Docker daemon did not become ready after $MaxAttempts attempts."
exit 1
