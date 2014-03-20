## 0.1.1

- Fix update-release-notes
  The previous version wasn't being passed.

- Remove lein-pallet-release from :plugins

- Refactor init
  Move init into the release namespace.  Pull init specific functions out of
  lein namespace.

- Add debug logging to lein and git functions

- Refactor into git and lein namespaces
  Put all git and lein commands into their respective namespaces.

- Make finish commit any modified files
  This is so that any modified files, e.g. source files modified by the 
  set-version plugin, are committed.

- Fix tag name for release
  The tag name incorrectly included a release/ prefix.

- Fix install instructions

- Improve readme

- Update README

## 0.1.0

- Add travis and release files

