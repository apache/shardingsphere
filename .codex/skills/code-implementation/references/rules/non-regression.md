<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Functional and Performance Non-Regression

Apply this gate to every task that changes a production, test, script, or other implementation artifact, including build logic, generated source, and behavior-affecting configuration. Verification must be proportionate to the affected behavior and credible cost risk; it must not become an unbounded demand to benchmark unrelated paths.

## Baseline and Supported Behavior

- The comparison baseline is the active task's original working-tree state captured before its first write, including pre-existing user changes. Do not rebaseline after a task write or after a later turn serving the same objective.
- Supported behavior is established by the user request, public and internal contracts, maintained documentation, focused tests, maintained production consumers, supported engine or dialect versions, registrations and loading paths, and externally observable success and failure semantics. An incidental implementation detail is not supported behavior merely because it existed; record evidence before excluding a plausible behavior from protection.
- The exact behavior or contract change required by the acceptance checklist and explicitly authorized for the active task is the intended change, not a functional regression. This exception applies only to that exact change and does not waive compatibility, performance, scope, or architecture gates.
- A pre-existing defect or slow path is not a task-introduced regression, but it cannot be used to conceal, average away, or excuse a new loss caused by the task.

## Functional Protection

1. Before the first relevant write, map the existing supported scenarios that the proposed change can affect, including successful paths, boundary inputs, failure behavior, compatibility surfaces, registrations, and indirect consumers when applicable.
2. Identify the evidence that protects each affected scenario. Use maintained focused tests and contract checks where they express the behavior; add or change tests only under `.codex/skills/code-implementation/references/rules/testing.md` and only when focused regression protection is necessary.
3. Do not weaken, delete, or rewrite a valid existing assertion, fixture, baseline, or contract merely to make changed behavior pass. First prove that it expresses the exact authorized behavior change or is itself incorrect under stronger contract evidence.
4. After the last relevant write, repeat the mapped checks against the same baseline and inspect the effective delta for untested losses. Any task-introduced loss, weakening, or incompatible change outside the exact authorized behavior is a blocking regression and must be repaired before handoff.

## Performance Risk and Evidence

1. Before the first relevant write, classify every affected existing supported path for credible performance risk by inspecting algorithmic complexity, call frequency, allocations and copies, serialization, parsing, I/O and network operations, database access, synchronization, concurrency, caching, resource lifecycle, and data-volume sensitivity where applicable.
2. When inspection rules out a credible cost increase, record the concrete reason and use focused functional verification; a benchmark is not required solely because code changed or a path is high-volume.
3. When a credible cost increase cannot be ruled out, capture the performance baseline before the first relevant write. Freeze the workload, representative inputs and sizes, dependencies, environment, warm-up, metrics, repetitions or sample count, and the decision threshold derived from measurement resolution and run-to-run variation. Use the same protocol after the last relevant write.
4. Use metrics owned by the affected path, such as latency distribution, throughput, CPU, allocation or memory, I/O count or volume, query count, lock contention, or asymptotic operation count. Do not substitute an unrelated aggregate metric.
5. A deterministic increase in algorithmic or resource cost that worsens a supported bound, or a reproducible deterioration beyond the frozen uncertainty threshold, is a performance regression. Repair it before handoff.
6. An improvement in one workload, percentile, metric, engine, or dialect cannot offset a regression in another affected supported path. Report and decide each protected path independently.
7. A feature or correctness change may be performance-neutral. An optimization experiment is retained only when its improvement exceeds run-to-run variation and correctness remains intact; remove a neutral, worse, or incorrect current-task optimization experiment without disturbing user work.
8. When the new behavior adds a new path, measure or reason about its effect on existing supported paths as well as its own cost. The absence of a historical result for the new path does not waive regression protection for the existing paths it shares.

Any later write that can affect a protected behavior, workload, build or runtime input, or measurement protocol invalidates the corresponding post-change evidence and requires the affected checks or measurements to be rerun.

## Evidence Failure

Passing compilation, unit tests, coverage, or a single benchmark sample does not by itself prove non-regression. Missing, incomparable, unstable, or inconclusive evidence required by the risk classification is not a pass. Repair the measurement or implementation when possible; otherwise stop and report the exact blocker, affected path, evidence already obtained, and smallest decision or scope needed.
