(ns leiningen.pallet-release.release
  "Release a palletops project"
  (:require
   [clojure.java.io :refer [file resource]]
   [clojure.string :as string :refer [trim]]
   [leiningen.core.eval :as eval]
   [leiningen.pallet-release.core :refer [fail fail-on-error]]
   [leiningen.pallet-release.git :as git]
   [leiningen.pallet-release.lein :as lein]
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

(defn do-start
  "Start a PalletOps release"
  [project old-version new-version]
  {:pre [(map? project)]}
  (lein/clean project)
  (lein/test project)
  (travis/enable project)
  (git/release-start new-version)
  (update-release-notes new-version)
  (lein/update-versions project old-version new-version)
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

(defn finish
  "Finish a PalletOps release"
  [project args]
  (let [new-version (new-version)]
    (lein/clean project)
    (lein/test project)
    (git/add "-u")
    (git/commit
     (str "Updated project.clj, release notes and readme for " new-version))
    (git/push "origin" (git/current-branch))
    (.delete (file ".pallet-release"))
    (println
     "Wait for travis to push to master,\n"
     "then run `lein pallet-release publish` to publish jars")))

(defn publish
  "Publish jars from master to clojars"
  [project args]
  (git/checkout "master")
  (git/pull)
  (lein/deploy project))
