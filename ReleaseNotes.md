## 0.1.17

- Fix publish command brocken in leinout refactoring

## 0.1.16

- Fix branch name used to push in finish

- Factor out leinout lib

## 0.1.15

- Don't error building doc profile when no git

## 0.1.14

- Make plugin work when there is no origin remote
  The doc plugin no longer errors when there is no origin remote.

## 0.1.13

- Allow use without project.clj
  e.g, when creating projects with lein new.

- Allow specification of :develop-branch
  Allow specification of :develop-branch in :pallet-release, so maintenance
  branches can be released.

  Closes #10

## 0.1.12

- Add wait command to wait on travis builds

- Add :doc-base profile
  Closes #12

- Add version command

- Add auth command
  Can be used to authorise the build-automation team on travis.
  Closes #11

- Poll travis api for build status
  Closes #13

## 0.1.11

- Run `lein check` on release start
  Check for compilation and reflection issues.

- Add lein/pom function

## 0.1.10

- Don't write :pallet-release on init
  The :pallet-release configuration is now optional.

- Fix no-checkouts profile

## 0.1.9

- Default :pallet-release push url from git origin
  When pushing from travis, default the pallet-release url and branch based
  on the git origin remote.

  Closes #7

## 0.1.8

- Clean before deploying jars

## 0.1.7

- Use github for repo name, and auth on init
  Authorise the build-automation team on init.  Use the origin repo to
  calculate the push repo url.

- Fix list of known commands in help message

## 0.1.6

- Fix profiles.clj merging
  Closes #4

- Add :release, :no-checkouts profiles in middleware

## 0.1.5

- Add support for projects using lein-modules
  Invokes all lein targets using modules if present in the :plugins.

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
