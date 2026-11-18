# Claude Code instructions — SemProject_MLEngine

- Before implementing Android features, read `Mobile-App/PLANTID_MOBILE_APP_SPEC.md`.
  It is a **local, git-ignored** file: never stage, commit, rename, delete or quote it
  in public docs.
- Android work belongs in `Mobile-App/` only. Do not create another git repository.
- Do not modify `Model_Base.ipynb`, `FloraDB_Exploration.ipynb`, `care_tips.json` or
  `species_map.json` unless explicitly asked.
- Work only within the approved sprint scope; one coherent feature at a time. Plan and
  get approval before substantial changes.
- UI-first: no real AI model integration until Sprint 7 and an agreed model contract.
  Mock outcomes must be clearly labelled demo data.
- Never invent model accuracy, confidence thresholds, plant facts or care advice.
- Explain why before adding any dependency; use `Mobile-App/gradle/libs.versions.toml`.
- Build and test from `Mobile-App/` (`./gradlew assembleDebug testDebugUnitTest`) before
  each commit, and report failures honestly.
- Commits: small and focused, prefixes `feat:` `fix:` `ui:` `refactor:` `test:` `docs:`
  `chore:`. Use feature branches; merge locally; never push, force-push or rewrite
  history without explicit approval.
- Record agreed decisions and open questions in `Mobile-App/docs/DECISIONS.md`.
