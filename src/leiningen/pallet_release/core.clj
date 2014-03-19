(ns leiningen.pallet-release.core)

(defn fail
  "Fail with the given message, msg."
  [msg]
  (throw (ex-info msg {:exit-code 1})))

(defn fail-on-error
  "Fail on a shell error"
  [exit]
  (when (and exit (pos? exit))
    (fail "Shell command failed")))
