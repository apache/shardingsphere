# Issue #38298 분석 및 수정 작업 기록

- 작성일: 2026-03-05
- 작성자: Codex 협업 세션
- 저장소: `apache/shardingsphere`
- 대상 이슈: https://github.com/apache/shardingsphere/issues/38298
- 작업 브랜치: `codex/issue_38298`
- 변경 파일: `.github/workflows/e2e-sql.yml`

## 1) 문제 정의

### 1-1. 이슈 제목

`ci: accidental actions/checkout failure in E2E-SQL` (Issue #38298)

### 1-2. 실제 장애 현상

E2E-SQL GitHub Actions run 중 일부 matrix job에서 `actions/checkout` 단계가 실패했다.

대표 실패 로그:

```text
/usr/bin/git -c protocol.version=2 fetch --no-tags --prune --no-recurse-submodules --depth=1 origin +48ffae5acbd53e554a99fe93ebd0cf6572a5a6a5:refs/remotes/pull/38292/merge
fatal: remote error: upload-pack: not our ref 48ffae5acbd53e554a99fe93ebd0cf6572a5a6a5
```

참조 run / job:
- Run: https://github.com/apache/shardingsphere/actions/runs/22547669595
- Failed job 예시: https://github.com/apache/shardingsphere/actions/runs/22547669595/job/65343007272

### 1-3. 중요한 관찰 포인트

같은 run 내부에서도,
- 어떤 job은 **같은 PR merge ref를 정상 checkout**했고,
- 어떤 job은 **`not our ref`로 실패**했다.

즉, 테스트 코드 자체 실패가 아니라 checkout 시점/대상 ref 해석 과정의 불안정성에 가깝다.

## 2) 원인 분석

### 2-1. 현재 checkout 동작 특성

`pull_request` 이벤트에서 `actions/checkout` 기본 동작은 synthetic merge commit 컨텍스트를 사용한다.
문제 상황에서는 특정 synthetic merge SHA를 직접 fetch하려고 하다가 `not our ref`가 발생했다.

### 2-2. 이슈 댓글 기반 외부 배경

이슈 코멘트에서 GitHub 변경 사항(2026-02-19)이 인용됨.
핵심은 test merge commit 생성/갱신 빈도 정책 변경이며, 특정 타이밍의 SHA fetch 불일치 가능성이 존재한다.

참고 링크(이슈 코멘트에 공유됨):
- https://github.blog/changelog/2026-02-19-changes-to-test-merge-commit-generation-for-pull-requests/

### 2-3. 재발성/우선순위 판단

최근 E2E-SQL 실패 run 샘플을 확인했을 때, `not our ref` 패턴은 다수 실패의 공통 원인이 아니라 **특정 케이스에서 간헐적으로 발생**하는 성격이었다.

해석:
- 빈도는 낮아도,
- 발생 시 테스트 자체 시작 전에 실패하므로 contributor 경험에 악영향이 크다.
- 따라서 작은 워크플로 수정으로 방어하는 가치가 있다.

## 3) 해결안 검토

### 3-1. 후보안 A (보류)

`ref: ${{ github.event.pull_request.head.sha }}`

보류 이유:
- base 최신 상태와 merge된 결과를 검증하지 못할 수 있어,
- CI가 통과해도 실제 merge 시 문제를 숨길 위험이 있음.

### 3-2. 후보안 B (중장기)

prepare 단계에서 소스 스냅샷을 artifact로 만들어 matrix job이 재사용.

장점:
- checkout 반복을 크게 줄여 flakiness 완화 가능.

단점:
- 구조 변경 범위가 큼.
- 현재 이슈는 checkout ref 안정화로 먼저 해결 가능한 수준.

### 3-3. 채택안 C (이번 작업)

모든 `actions/checkout`에 `ref: ${{ github.ref }}`를 명시.

의도:
- PR 이벤트에서 `github.ref`는 `refs/pull/<num>/merge` 형식.
- SHA 직접 fetch 경로 의존도를 낮추고, merge ref 기반으로 checkout 안정성을 높인다.
- `workflow_dispatch`에서도 `github.ref`는 유효(수동 실행 시점 ref)라 동작 호환성 유지.

## 4) 실제 수정 내역

대상 파일: `.github/workflows/e2e-sql.yml`

아래 5개 checkout step 모두 동일하게 수정:
- `Detect Changes and Generate Matrix` job
- `Prepare E2E Artifacts` job
- `E2E - SQL (Smoke)` job
- `Detect Remaining Matrix` job
- `E2E - SQL (Stage 2)` job

변경 패턴:

```yaml
- uses: actions/checkout@v6.0.1
  with:
    ref: ${{ github.ref }}
```

핵심 포인트:
- 워크플로 논리/매트릭스 생성 로직/테스트 명령은 변경하지 않음.
- checkout ref 지정만 추가한 최소 변경(minimal patch).

## 5) 검증 절차 및 결과

### 5-1. 사전 검증

- 대상 이슈 상태 확인: OPEN, 미할당
- 관련 PR/중복 fix 여부 검색: 즉시 참조 가능한 중복 PR 없음
- 실패 run 로그 확인: `not our ref` 재현 로그 확인 완료

### 5-2. 변경 검증

- `git diff`로 변경 scope 확인: `e2e-sql.yml` checkout 설정만 수정됨
- YAML 구조 파손 여부: 들여쓰기/키 구조 수동 검토 완료

주의:
- 실제 효용성 최종 확인은 GitHub Actions 실런(특히 PR matrix)에서 필요

## 6) 리스크 평가

- 낮은 리스크:
  - checkout `ref` 명시 추가만 수행
  - 테스트 실행 커맨드/빌드 캐시/아티팩트 로직 불변

- 잔여 리스크:
  - 본 이슈가 GitHub 인프라 측 간헐 이슈이면 100% 제거는 장담 불가
  - 다만 동일 class의 SHA-fetch 실패 가능성을 줄이는 방향은 타당

## 7) PR 설명 시 사용할 요약 문구

- "This patch explicitly checks out `github.ref` in all E2E-SQL jobs to prefer PR merge-ref checkout over implicit SHA fetch path, reducing accidental `not our ref` failures while preserving merged-with-base validation semantics."

## 8) 후속 제안 (선택)

필요 시 다음 확장 검토 가능:
- 동일 패턴을 `e2e-agent.yml`, `e2e-operation.yml`에도 점진 적용
- 중장기적으로 source snapshot 재사용 구조 검토

