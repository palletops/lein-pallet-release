(ns leiningen.pallet-release.release
  "Release a palletops project"
  (:require
   [clojure.java.io :refer [file resource]]
   [clojure.string :as string :refer [trim]]
   [fipp.edn :refer [pprint]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [info]]
   [leiningen.pallet-release.core
    :refer [deep-merge fail fail-on-error release-config]]
   [leiningen.pallet-release.git :as git]
   [leiningen.pallet-release.lein :as lein]
   [leiningen.pallet-release.travis :as travis])
  (:import
   java.io.File))

(defn add-release-notes-md
  []
  (fail-on-error (eval/sh "touch" "ReleaseNotes.md"))
  (git/add "ReleaseNotes.md"))

(defn release-profiles [project]
  {:dev {:plugins '[[lein-pallet-release "0.1.2"]]
         :pallet-release (release-config project)}
   :no-checkouts {:checkout-deps-shares ^:replace []}
   :release {:set-version
             {:updates [{:path "README.md" :no-snapshot true}]}}})

(defn lein-init
  "Initialise project for release"
  [project]
  (let [f (lein/profiles-clj-file project)
        profiles (deep-merge
                  (release-profiles project)
                  (lein/read-profiles f))]
    (info "Writing" (.getPath f))
    (spit f
          (binding [*print-meta* true]
            (with-out-str (pprint (release-profiles project)))))))

(defn init
  "Initialise the project for release via travis."
  [project [token]]
  (when-not (and token (= 40 (count token)))
    (fail "init expects a 40 character github token to be used to push."))
  (git/ensure-origin)
  (git/ensure-git-flow)
  (add-release-notes-md)
  (lein-init project)
  (travis/init project token)
  (println "Next:")
  (println "a) Commit .travis.yml, profiles.clj and ReleaseNotes.md")
  (println "b) Ensure that pbors has write access to the github repo"))

(defn update-release-notes
  [old-version new-version]
  (let [f (File/createTempFile "updateNotes" ".sh")]
    (try
      (spit
       f
       (slurp (resource "leiningen/pallet_release/update_release_notes.sh")))
      (fail-on-error (eval/sh "bash" (.getPath f) old-version new-version))
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
  (update-release-notes old-version new-version)
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
