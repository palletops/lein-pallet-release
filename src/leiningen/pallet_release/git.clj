(ns leiningen.pallet-release.git
  (:require
   [clojure.string :as string :refer [blank? trim]]
   [leiningen.core.eval :as eval]
   [leiningen.pallet-release.core :refer [fail fail-on-error]]))

(defn ensure-origin
  []
  (when (pos? (eval/sh "git" "remote" "show" "origin"))
    (fail "No origin repository.  Have you created it on github?")))

(defn ensure-git-flow
  []
  (let [m (with-out-str
            (eval/sh "git" "config" "--get" "gitflow.branch.master"))]
    (when (blank? m)
      (fail-on-error (eval/sh "git" "flow" "init" "-d")))))

(defn add-release-notes-md
  []
  (fail-on-error (eval/sh "touch" "ReleaseNotes.md"))
  (fail-on-error (eval/sh "git" "add" "ReleaseNotes.md")))

(defn add-remote
  [remote url]
  (fail-on-error (eval/sh "git" "remote" "add" remote url)))

(defn tag
  [& args]
  (fail-on-error (apply eval/sh "git" "tag" args)))

(defn add
  [& args]
  (fail-on-error (apply eval/sh "git" "add" args)))

(defn commit
  [msg]
  (fail-on-error (eval/sh "git" "commit" "-m" msg)))

(defn push
  [remote branch-spec]
  (fail-on-error (eval/sh "git" "push" remote branch-spec)))

(defn current-branch
  []
  (let [sha (trim (with-out-str (fail-on-error
                                 (eval/sh "git" "rev-parse" "HEAD"))))
        out (with-out-str (fail-on-error
                           (eval/sh "git" "branch" "-v" "--no-abbrev")))
        line (->> out
                  (string/split-lines)
                  (filter #(.contains % sha))
                  (remove #(.contains % "detached"))
                  first
                  )]
    (->> (string/split (string/replace line "*" "") #" +")
         (remove blank?)
         first)))

(defn config
  [kvs]
  (doseq [[k v] kvs]
    (fail-on-error (eval/sh "git" "config" "--global" (name k) v))))
