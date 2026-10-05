# Report verification — 4 October 2026

## Fresh backend verification

Command:

```bash
cd backend
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./mvnw --batch-mode --no-transfer-progress test
```

Result: BUILD SUCCESS; 28 tests, 0 failures, 0 errors, 0 skipped.
Flyway validated and applied 28 migrations through version 1.2.7 against a clean
PostgreSQL/pgvector Testcontainers database. Docker Engine: 29.8.1. JDK: 21.

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| JwtUtilsTest | 1 | 0 | 0 | 0 |
| AiCourseServiceTest | 1 | 0 | 0 | 0 |
| ForgotPasswordUseCaseTest | 3 | 0 | 0 | 0 |
| ProgressConcurrencyIntegrationTest | 23 | 0 | 0 | 0 |

The run finished at 2026-10-04T22:45:07+07:00. It used the existing working tree
based on commit 3a71b37, including pre-existing uncommitted application changes.
The report revision changed report files only. Current backend tests are fresh;
the previous application build and E2E records are not fresh verification.

## Historical evidence retained

- Android XML records dated 3 October 2026: two tests, both passing (login mapping
  and shared token refresh). These were inspected, not rerun in this report task.
- September live HTTP journey: 26 checks with no failures, as recorded in
  docs/reliable-progress-verification.md. Not rerun against current payment/recovery changes.
- September emulator and release-build results: historical; no new app build is claimed.
- September monitoring smoke and alert lifecycle: historical, from docs/monitoring-runbook.md.
- The existing Grafana screenshot is a historical local capture, not a new performance measurement.

## Artifact checks — second report revision

- Compiled successfully with Tectonic 0.17.0 (LaTeX, BibTeX, reference reruns, PDF generation).
- Final PDF: 105 A4 pages, 29 figures (28 editable diagrams and one historical
  Grafana capture), and 28 tables.
- No compiler warnings, undefined references, missing citations, duplicate labels,
  overfull boxes, or underfull paragraph warnings in the final build.
- Inspected the one-page abstract, redrawn deployment/domain views, assessment and
  reward models, quiz workflow, worked progression table, API reference, and proposed
  restoration diagram. Increased node gaps and reduced awkward word breaks.
- Preserved the supplied author, university, student ID, supervisor, and submission month.
- The second pass changes report files only. Application findings in source-audit.md
  are documented, not implemented; no new backend or Android test run is claimed.
- Local validation uses Tectonic's XeTeX engine. Standard pdfLaTeX compilation is
  documented for Overleaf, but native pdfLaTeX/Overleaf was not run in this environment.

### Source fingerprints

| File | SHA-256 |
|---|---|
| main.tex | 7b142b0424d106afbd3f6cf30380195004136db5bde93ddb5d34e617290f5ba0 |
| diagrams.tex | 53e0bb113e3b10274b7405f195e117ab6233f9bbdc6707d4535acd1946ad2308 |
| references.bib | a03cb241d55abd2a9da8f31451c61d0ddc94fb352876319557857069eff914a2 |
| source-audit.md | 9b49da1502a9d1e4f75b028dc64b118ced7b9b65c8030ee52ace4c55a6ad31c0 |
| figures/grafana-service-overview.png | 8c3aa52ddd5ce7a3798d3c4b3e4c26005f86d7f538a293e024b3e0c689e8664e |

- Independently extracted and compiled the archive: passed without compiler warnings.
- Extracted PDF text matches saved main.pdf exactly, including page breaks.
- Archive membership is explicitly allowlisted; no credentials or scratch files are bundled.
