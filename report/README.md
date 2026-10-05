# EduFlex project report

This directory contains the English EduFlex technical report. `main.tex` is the main
source; `main.pdf` is the compiled reading copy. `eduflex-overleaf-report.zip` contains
the self-contained source package for Overleaf.

The October revision explains design decisions and alternatives, database constraints
and connection budgets, Android refresh coordination, password recovery, development
checkout, transactional concurrency, cache races, message-delivery limits, AI retrieval
parameters, monitoring semantics, and a prioritized engineering roadmap. The second
review adds assessment and reward models, a comparison of the actual quiz workflows,
a proposed restoration diagram, worked XP orderings, concrete API access rules,
source entry points, and a repeatable performance evaluation protocol. Deployment
and domain overview diagrams were redrawn for readability.

`verification.md` distinguishes the fresh 4 October backend results from historical
Android, HTTP, and monitoring checks. `review-notes.md` records the report issues fixed
and the remaining evaluation gaps.
`source-audit.md` records application gaps discovered during report review, including
incomplete quiz validation, client-reported quest activity, order-sensitive course
rewards, mismatched entitlement rules, missing cache invalidation, and the retired
default generation model. These findings are documented, not implemented as code fixes.

## Use in Overleaf

1. Upload the generated ZIP as a new Overleaf project, or upload this directory.
2. Set main.tex as the main document.
3. Use pdfLaTeX as the compiler.
4. Confirm the title-page identity fields and institution requirements.
5. Optionally add annotated Android device
   screenshots after repeating the documented emulator journey.
6. Compile twice, with BibTeX between LaTeX passes if Overleaf does not run it
   automatically.

The file diagrams.tex contains reusable figure commands and must remain beside
main.tex. All architectural diagrams compile directly in Overleaf. The included
Grafana image is a real local capture; the report explicitly records the current lack
of submission-quality Android captures instead of presenting mock screenshots.

Typical local compilation is: pdflatex main, bibtex main, pdflatex main, and
pdflatex main again.

Run those commands from this directory:

```bash
pdflatex -interaction=nonstopmode -halt-on-error main.tex
bibtex main
pdflatex -interaction=nonstopmode -halt-on-error main.tex
pdflatex -interaction=nonstopmode -halt-on-error main.tex
```

Tectonic can also run the LaTeX/BibTeX passes automatically:

```bash
tectonic --keep-logs main.tex
```

The report uses standard pdfLaTeX-compatible packages and editable TikZ diagrams;
Overleaf does not require Tectonic. The archive contains source, bibliography,
diagrams, the Grafana image, and documentation. Credentials and generated scratch
files are excluded.

The report calls CI release outputs unsigned inspection artifacts. Do not describe
them as store-ready unless release signing and distribution have been configured.
Before submission, customize the university, faculty, student, student ID,
supervisor, date, and declaration.
