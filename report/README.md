# EduFlex project report

This directory is an English LaTeX report prepared for Overleaf. The manuscript is
based on the implemented EduFlex repository and its recorded verification results.
Its native TikZ diagram set covers context, application layers, Android navigation,
deployment, data relationships, API security, authentication, learning workflows,
caching, messaging, AI retrieval, CI, and monitoring.

## Use in Overleaf

1. Upload the generated ZIP as a new Overleaf project, or upload this directory.
2. Set main.tex as the main document.
3. Use pdfLaTeX as the compiler.
4. Replace the title-page placeholders in main.tex.
5. Replace the title-page identity fields and optionally add annotated Android device
   screenshots after repeating the documented emulator journey.
6. Compile twice, with BibTeX between LaTeX passes if Overleaf does not run it
   automatically.

The file diagrams.tex contains reusable figure commands and must remain beside
main.tex. All architectural diagrams compile directly in Overleaf. The included
Grafana image is a real local capture; the report explicitly records the current lack
of submission-quality Android captures instead of presenting mock screenshots.

Typical local compilation is: pdflatex main, bibtex main, pdflatex main, and
pdflatex main again.

The report calls CI release outputs unsigned inspection artifacts. Do not describe
them as store-ready unless release signing and distribution have been configured.
Before submission, customize the university, faculty, student, student ID,
supervisor, date, and declaration.
