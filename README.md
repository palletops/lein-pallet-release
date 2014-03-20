# lein-pallet-release

[![Build Status](https://travis-ci.org/palletops/lein-pallet-release.png?branch=develop)](https://travis-ci.org/palletops/lein-pallet-release)

A Leiningen plugin to release PalletOps projects.

## Usage

Add the plugin to your `:user` `:dependencies` in `~/.lein/profiles.clj`:

```clj
:dependencies [[lein-pallet-release "0.1.0"]]
```

### init

The `init` subcommand is used to initialise a project for releasing
with a test on travis.  It requires a single argument, a 40 digit hex
github key, that will be used to authorise travis to push to github
master if the build succeeds.

```
lein pallet-release init 435afe787ab878c32432435afe787ab878c32432
```

This will write the `.travis.yml` file if it doesn't exist.  It will also
add the plugin and configuration to `profiles.clj`.

After running the command, inspect the created files and commit them.

You need to ensure the `pbors` github user has permissions on the repository.

Assumes that the
[travis command line](http://blog.travis-ci.com/2013-01-14-new-client/)
is installed.

### start

To start a release, run

```
lein pallet-release start previous-release new-release
```

This will create a release branch, enable the project on travis, and
update release notes, readme, etc.

After running, check the modifications.  Anything staged will be
commited by the `finish` command.

### finish

To finish a release, run

```
lein pallet-release finish
```

This will commit any staged files with a commit message including the version number.

It then pushes the branch to github, from where travis will build it.

### publish

When the travis build has successfully completed, publish the jars with:

```
lein pallet-release publish
```

## License

Copyright © 2014 Hugo Duncan, Antoni Batchelli

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
