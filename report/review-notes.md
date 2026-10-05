# Report review and revision — 4 October 2026

## Issues resolved

- Reconciled the abstract and evaluation chapter using one source of truth for backend
  tests (28), PostgreSQL scenarios (23), and Flyway migrations (28).
- Distinguished fresh backend checks, 3 October Android unit-test records, and
  historical September HTTP/emulator/release/monitoring checks.
- Explained the modular backend, authoritative versus derived state, implementation
  choices, rejected alternatives, and concrete costs.
- Added detailed data cardinality, natural keys, partial successful-payment uniqueness,
  query/index considerations, and runtime versus test connection budgets.
- Documented synchronized Android refresh, bounded retries, network failure,
  reset-token hashing/consumption, SMTP configuration, and cross-system limits.
- Described checkout as a development simulation with repeat-safe records, rather than
  implying a functioning MoMo settlement integration.
- Expanded the transaction argument, SQL excerpts, concurrent request ordering, calendar
  rules, queue delivery semantics, broker persistence limits, and stale-vector races.
- Corrected AI retrieval terminology: one vector per lesson, five retrieved lessons,
  cosine score, bounded excerpts, fallback ordering, and optional external generation.
- Added monitoring thresholds, counter/timer definitions, investigation flow, and
  recovery boundaries.
- Added eight editable TikZ diagrams and improved API authorization flow.
- Added acceptance criteria to the future roadmap rather than listing technology names.

## Second review: substantive corrections

- Shortened the abstract to one readable page while expanding the engineering body.
- Distinguished implemented source, executed verification, historical evidence, and
  proposed acceptance/recovery models.
- Corrected the two quiz workflows: fill-blank is currently feedback-only, with
  weaker scope validation, while multiple choice owns persisted progress/rewards.
- Added the submitted-answer denominator defect, duplicate/subset risks, missing
  answer-key error handling, and required regression evidence.
- Explained top-level progress calculation and worked through source-derived A/B/Q
  orderings that expose missed course reward eligibility.
- Distinguished daily check-in from study streak, seeded quest targets, protected
  reward transitions from authentic activity, and unverified certificates/predictors.
- Added concrete route access rules and source entry points; documented inconsistent
  review eligibility and missing content-read entitlement checks.
- Corrected cache descriptions: configured TTL names are not all active paths;
  nested course caches, new lesson lists, and enrolment-filtered searches have missing
  invalidation dependencies. TTL is not a cache size limit.
- Documented cleartext/backup/session defaults and best-effort study-time delivery.
- Distinguished clean migrations from populated upgrades and clarified deletion,
  external media, retention, and transaction rollback boundaries.
- Verified the retired Gemini default against Google's release notes; documented
  ONNX inference, short-text/language constraints, and synchronous query embeddings.
- Added state-specific recovery, immutable environment provenance, and a concrete
  planned performance measurement protocol without invented results.
- Added four diagrams and redrew the deployment/domain overview to remove crossings.
- Added `source-audit.md` so a later application task can act on precise findings.

## Remaining evaluation gaps

The report does not fabricate usability findings, course effectiveness, cache speedups,
sustained-load results, AI groundedness scores, real payments, SMTP end-to-end delivery,
production high availability, or signed store delivery. These require separate
implementation or experiments. Android screenshots still require a real device capture
with non-sensitive demonstration data; the Grafana capture is retained as historical
visual evidence.

The application and infrastructure files already had uncommitted changes when this
review began. They were preserved. This task edits report material and rebuilds its
PDF/archive; it does not implement the roadmap's application changes.
