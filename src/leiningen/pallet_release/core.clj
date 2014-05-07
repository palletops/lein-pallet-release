(ns leiningen.pallet-release.core
  (:require
   [clojure.string :as string :refer [trim]]))

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
