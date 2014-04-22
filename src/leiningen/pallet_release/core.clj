(ns leiningen.pallet-release.core
  (:require
   [clojure.string :as string :refer [trim]]))

(defn deep-merge
  "Recursively merge maps."
  [& ms]
  (letfn [(f [a b]
            (if (and (map? a) (map? b))
              (deep-merge a b)
              (if (and (vector? a) (vector? b))
                (vec (concat a b))
                (or b a))))]
    (apply merge-with f ms)))

(defn fail
  "Fail with the given message, msg."
  [msg]
  (throw (ex-info msg {:exit-code 1})))

(defn fail-on-error
  "Fail on a shell error"
  [exit]
  (when (and exit (pos? exit))
    (fail "Shell command failed")))

(defn release-notes [version]
  (let [re1 (re-pattern (str
                         "#+ "
                         version
                         "\n(?:\n+[^#\n]+[^\n]+)+"))
        re2 (re-pattern (str "(?s)#+ "
                             version
                             "\n+(.*)"))
        notes (re-find re1 (slurp "ReleaseNotes.md"))
        notes (second (re-find re2 notes))]
    (trim notes)))
