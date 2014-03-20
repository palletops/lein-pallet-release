previous_version=$1
version=$2
export previous_version

function log-without () {
    export previous_version
    git --no-pager log --format=%H $previous_version.. |
    grep -v -f <(git --no-pager log --format=%H \
                     --grep=Merge --grep=travis \
                     --grep='Updated version for next' \
                     --grep='Updated project.clj, release notes and readme' \
                     --grep='release script' --grep='Update project version' \
                     $previous_version.. ) |
    git log --pretty=changelog --stdin --no-walk
}

tmpfile=$(mktemp releaseXXXXX)
trap "rm -f $tmpfile" EXIT
printf "## ${version}\n\n$(log-without)\n\n" |
  cat - ReleaseNotes.md > $tmpfile && mv -f $tmpfile ReleaseNotes.md || exit 1
