(ns leiningen.pallet-release.release
  "Release a palletops project"
  (:require
   [clojure.java.io :refer [file resource]]
   [clojure.string :as string :refer [trim]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [apply-task]]
   [leiningen.pallet-release.core :refer [fail fail-on-error]]
   [leiningen.pallet-release.travis :as travis])
  (:import
   java.io.File))

(defn update-release-notes
  [new-version]
  (let [f (File/createTempFile "updateNotes" ".sh")]
    (try
      (spit
       f
       (slurp (resource "leiningen/pallet_release/update_release_notes.sh")))
      (fail-on-error (eval/sh "bash" (.getPath f) new-version))
      (finally
        (if (.exists f)
          (.delete f))))))

(defn test-project
  [project]
  (apply-task "clean" project [])
  (apply-task "with-profile" project ["+no-checkouts" "test"]))

(defn branch-for-release
  [new-version]
  (fail-on-error (eval/sh "git" "flow" "release" "start" new-version)))

(defn update-versions
  [project old-version new-version]
  (apply-task "with-profile" project
              ["+release" "set-version"
               new-version ":previous-version" old-version]))

(defn do-start
  "Start a PalletOps release"
  [project old-version new-version]
  {:pre [(map? project)]}
  (test-project project)
  (travis/enable project)
  (branch-for-release new-version)
  (update-release-notes new-version)
  (update-versions project old-version new-version)
  (spit ".pallet-release" new-version)
  (println
   "Check project.clj, ReleaseNotes and README (finish will commit these)"))

(defn start
  "Start releasing a PalletOps project"
  [project [old-version new-version]]
  (when-not (and old-version new-version)
    (fail "start must be called with previous and new release versions."))

  (do-start project old-version new-version))

(defn new-version
  []
  (let [f (file ".pallet-release")]
    (when-not (.exists f)
      (fail "No release started (use lein pallet-release start)"))
    (slurp f)))

(defn commit-release-files
  [new-version]
  (fail-on-error
   (eval/sh "git" "add" "project.clj" "ReleaseNotes.md" "README.md"))
  (fail-on-error
   (eval/sh "git" "commit" "-m"
            (str "\"Updated project.clj, release notes and readme for "
                 new-version "\""))))

(defn push-release-branch
  []
  (let [branch (trim
                (with-out-str
                  (fail-on-error
                   (eval/sh "git" "rev-parse" "--abbrev-ref" "HEAD"))))]
    (fail-on-error (eval/sh "git" "push" "origin" branch))))


(defn finish
  "Finish a PalletOps release"
  [project args]
  (let [new-version (new-version)]
    (test-project project)
    (commit-release-files new-version)
    (push-release-branch)
    (.delete (file ".pallet-release"))
    (println
     "Wait for travis to push to master,\n"
     "then run `lein pallet-release publish` to publish jars")))

(defn pull-master
  []
  (fail-on-error (eval/sh "git" "checkout" "master"))
  (fail-on-error (eval/sh "git" "pull")))

(defn deploy
  [project]
  (apply-task "deploy" project ["clojars"]))

(defn publish
  "Publish jars from master to clojars"
  [project args]
  (pull-master)
  (deploy project))
