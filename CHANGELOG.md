# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Initial project structure for chunk-learn RSVP CLI.
- Play/Pause mechanism (Spacebar) during RSVP playback.

### Changed
- Refactored project to `com.chunkrsvp` package structure.
- Standardized project artifact and entry point names to `ChunkRSVP`.
- Improved test/production code alignment.
- Decoupled configuration management into a unified `ConfigurationManager` service [RSVP-010].
- Extracted UI/Terminal management into `ViewManager` and `AnsiTerminalView` [RSVP-011].
- Decoupled interactive input handling into `InputController` and `InputAction` [RSVP-012].
- Implemented flexible input source handling: prioritizes file path over piped input [RSVP-004].
