# LSP Phase 0 Browser Automation

This folder is reserved for browser-side LSP performance automation artifacts.

Current implementation uses:
- `scripts/lsp-phase0-benchmark.sh` as the executable benchmark harness
- `chromedriver` + Chrome DevTools/WebDriver API (no npm dependencies required)

Outputs are written to:
- `target/lsp-phase0/metrics.jsonl`
- `target/lsp-phase0/summary.json`

Use:
- `./scripts/run-phase0-automated-checks.sh`
