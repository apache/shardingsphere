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
# TODO Once https://github.com/microsoft/Windows-Containers/pull/602 is merged we can remove this script.
$myWindowsID = [System.Security.Principal.WindowsIdentity]::GetCurrent()
$myWindowsPrincipal = new-object System.Security.Principal.WindowsPrincipal($myWindowsID)
$adminRole = [System.Security.Principal.WindowsBuiltInRole]::Administrator
if (-not $myWindowsPrincipal.IsInRole($adminRole))
{
    throw "You must run this script as administrator"
}
if (Get-Service -Name docker -ErrorAction SilentlyContinue)
{
    $containers = docker ps -aq 2> $null
    if ($containers)
    {
        docker stop $containers 2> $null | Out-Null
        docker rm -f $containers 2> $null
    }
    $images = docker images -q 2> $null
    if ($images)
    {
        docker rmi -f $images 2> $null
    }
    $volumes = docker volume ls -q 2> $null
    if ($volumes)
    {
        docker volume rm -f $volumes 2> $null
    }
    $networks = docker network ls --format "{{.Name}}" 2> $null
    if ($networks)
    {
        $customNetworks = @()
        foreach ($network in $networks)
        {
            if ($network -ne "bridge" -and $network -ne "host" -and $network -ne "none" -and $network -ne "nat")
            {
                $customNetworks += $network
            }
        }
        if ($customNetworks.Count -gt 0)
        {
            foreach ($network in $customNetworks)
            {
                docker network rm $network 2> $null
            }
        }
    }
    Stop-Service -Name docker -Force -ErrorAction Stop
    & sc.exe delete docker 2>&1
}
$registryPaths = @(
    "HKLM:\SYSTEM\CurrentControlSet\Services\docker",
    "HKLM:\SYSTEM\ControlSet002\Services\docker",
    "HKLM:\SYSTEM\CurrentControlSet\Services\EventLog\Application\docker"
)
foreach ($regPath in $registryPaths)
{
    if (Test-Path $regPath)
    {
        Remove-Item $regPath -Recurse -Force
    }
}
$dockerExe = Join-Path $env:windir "System32\docker.exe"
if (Test-Path $dockerExe)
{
    Remove-Item $dockerExe -Force
}
$dockerdExe = Join-Path $env:windir "System32\dockerd.exe"
if (Test-Path $dockerdExe)
{
    Remove-Item $dockerdExe -Force
}
if (-not (Test-Path "$( $env:ProgramData )\docker"))
{
    exit 0
}
$services = @("cexecsvc", "vmcompute", "vmicguestinterface", "vmicheartbeat", "vmickvpexchange", "vmicrdv", "vmicshutdown", "vmictimesync", "vmicvmsession", "vmicvss")
foreach ($serviceName in $services)
{
    $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
    if ($service -and $service.Status -eq 'Running')
    {
        Stop-Service -Name $serviceName -Force -ErrorAction SilentlyContinue
    }
}
Start-Sleep -Seconds 2
$hcsdiagOutput = & hcsdiag.exe list 2> $null
if ($hcsdiagOutput)
{
    $containerMatches = $hcsdiagOutput | Select-String -Pattern "container" -SimpleMatch
    if ($containerMatches)
    {
        $containerMatches | ForEach-Object {
            $line = $_.Line
            if ($line -match '\{([^}]+)\}')
            {
                $containerId = $Matches[1]
                & hcsdiag.exe kill $containerId 2> $null
            }
        }
    }
}
if (Get-Command Get-ComputeProcess -ErrorAction SilentlyContinue)
{
    $computeProcesses = Get-ComputeProcess -ErrorAction SilentlyContinue
    if ($computeProcesses)
    {
        $containerProcesses = $computeProcesses | Where-Object { $_.Type -like "*container*" }
        if ($containerProcesses)
        {
            $containerProcesses | ForEach-Object {
                $_ | Stop-ComputeProcess -Force -ErrorAction SilentlyContinue
            }
        }
    }
}
Start-Sleep -Seconds 3
$windowsFilterPath = Join-Path "$( $env:ProgramData )\docker" "windowsfilter"
if (Test-Path $windowsFilterPath)
{
    $layerDirs = Get-ChildItem -Path $windowsFilterPath -Directory -ErrorAction SilentlyContinue
    if ($layerDirs)
    {
        $job = Start-Job -ScriptBlock {
            param($layers)
            Add-Type -TypeDefinition @"
using System;
using System.Runtime.InteropServices;
public class Hcs
{
[DllImport("ComputeStorage.dll", SetLastError=true, CharSet=CharSet.Unicode)]
public static extern int HcsDestroyLayer(string layerPath);
}
"@
            $results = @()
            foreach ($layer in $layers)
            {
                try
                {
                    $result = [Hcs]::HcsDestroyLayer($layer.FullName)
                    $results += [PSCustomObject]@{
                        Layer = $layer.Name
                        Path = $layer.FullName
                        Result = $result
                        Success = ($result -eq 0)
                    }
                }
                catch
                {
                    $results += [PSCustomObject]@{
                        Layer = $layer.Name
                        Path = $layer.FullName
                        Result = -1
                        Success = $false
                        Error = $_.Exception.Message
                    }
                }
            }
            return $results
        } -ArgumentList @(,$layerDirs)
        if ($job | Wait-Job -Timeout 120)
        {
            $results = $job | Receive-Job
            $job | Remove-Job
        }
        else
        {
            $job | Stop-Job
            $job | Remove-Job
        }
    }
    if (Test-Path $windowsFilterPath)
    {
        & cmd.exe /c "rd /s /q `"$windowsFilterPath`"" 2> $null | Out-Null
        if (Test-Path $windowsFilterPath)
        {
            $tempEmptyDir = Join-Path $env:TEMP "EmptyDir_$( Get-Random )"
            try
            {
                New-Item -ItemType Directory -Path $tempEmptyDir -Force | Out-Null
                & robocopy.exe $tempEmptyDir $windowsFilterPath /MIR /R:1 /W:1 /NP /NFL /NDL /NJH /NJS 2> $null | Out-Null
                Remove-Item $tempEmptyDir -Force -ErrorAction SilentlyContinue
            }
            catch
            {
                Remove-Item $tempEmptyDir -Force -ErrorAction SilentlyContinue
            }
        }
    }
}
Remove-Item "$( $env:ProgramData )\docker" -Recurse -Force
$dockerDownloads = "$env:UserProfile\DockerDownloads"
if (Test-Path $dockerDownloads)
{
    Remove-Item $dockerDownloads -Recurse -Force
}
