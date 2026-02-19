# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

This project is a fork of
[JFS Accounting](https://sourceforge.net/projects/jfsaccounting/),
diverging from upstream version 2.2-SNAPSHOT.

## [Unreleased]

### Added
- Modernization plan (`MODERNIZATION.md`) documenting a phased approach to
  bring the codebase from Java 5/6-era style to modern Java.
- `AGENTS.md` with build, test, lint commands and code style guidelines for
  AI-assisted development.
- GitHub Actions CI/CD workflow (`ci.yml`):
  - Pull request builds with `mvn clean install` on Ubuntu/JDK 21.
  - Release builds on push to master for Linux, Windows, and macOS.
  - Native installer creation via jpackage (AppImage, MSI, DMG).
  - Smoke tests for all three platform installers.
- AppImage build support for Linux distribution.

### Fixed
- CI: use `target/dist` for AppImage build output.
- CI: use bash shell for Maven build and fix installer test paths.
- CI: install jpackage dependencies on Linux runner.
- CI: upgrade deprecated GitHub Actions from v3 to v4.
- Excluded build artifacts from git tracking.
