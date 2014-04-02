## 0.1.4

- Improve release start output

- Show release notes on release start

- Annotate release tag with release notes

- Suppress git flow release start output

- Add space to unknown repo format message

## 0.1.3

- Merge to master, rather than fastforward

## 0.1.2

- Only push repositories on release/ branches
  If travis is building anything other than a release/ branch, it
  shouldn't push the repo.

- Set :description in project.clj

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

## 0.1.0

- Initial release
