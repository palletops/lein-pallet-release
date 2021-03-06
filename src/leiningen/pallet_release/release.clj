(ns leiningen.pallet-release.release
  "Release a palletops project"
  (:refer-clojure :exclude [test])
  (:require
   [clojure.java.io :refer [file resource]]
   [clojure.string :as string :refer [trim]]
   [com.palletops.leinout.core :refer [deep-merge fail fail-on-error]]
   [com.palletops.leinout.git :as git]
   [com.palletops.leinout.git-flow :as git-flow]
   [com.palletops.leinout.github :as github]
   [com.palletops.leinout.lein :as lein]
   [com.palletops.leinout.travis :as travis-api]
   [fipp.edn :refer [pprint]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [info]]
   [leiningen.pallet-release.core :refer [release-notes]]
   [leiningen.pallet-release.github
    :refer [auth-builder repo-coordinates]]
   [leiningen.pallet-release.lein :refer [check test update-versions]]
   [leiningen.pallet-release.travis :as travis])
  (:import
   java.io.File))

(defn add-release-notes-md
  []
  (fail-on-error (eval/sh "touch" "ReleaseNotes.md"))
  (git/add "ReleaseNotes.md"))

(defn release-config
  "Return a pallet release configuration map"
  [project origin]
  {:url (or (-> project :pallet-release :url)
            (repo-coordinates origin))
   :branch (or (-> project :pallet-release :branch)
               "master")})

(defn release-profiles [project origin]
  {:dev {:plugins '[[lein-pallet-release "RELEASE"]]}})

(defn lein-init
  "Initialise project for release"
  [project origin]
  (let [f (lein/profiles-clj-file project)
        profiles (deep-merge
                  (release-profiles project origin)
                  (lein/read-profiles f))]
    (info "Writing" (.getPath f))
    (spit f
          (binding [*print-meta* true]
            (with-out-str (pprint profiles))))))

(defn init
  "Initialise the project for release via travis."
  [project [token]]
  (when-not (and token (= 40 (count token)))
    (fail "init expects a 40 character github token to be used to push."))
  (git/ensure-origin)
  (git-flow/ensure-git-flow)
  (add-release-notes-md)
  (let [origin (git/origin)]
    (lein-init project origin)
    (auth-builder origin (github/github-login)))
  (travis/init project token)
  (println "Next:")
  (println
   "a) Commit .travis.yml, profiles.clj and ReleaseNotes.md (unless unchanged)")
  (println
   "b) Ensure that pbors has write access to the github repo"))

(defn auth
  "Authorise builder on github"
  [project _]
  (let [origin (git/origin)]
    (pprint (auth-builder origin (github/github-login)))
    (flush)))

(defn update-release-notes
  [old-version new-version]
  {:pre [old-version new-version]}
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
  {:pre [(map? project) old-version new-version]}
  (lein/clean project)
  (test project)
  (check project)
  (println)
  (git-flow/release-start new-version)
  (update-versions project old-version new-version)
  (lein/pom project) ; checks for snapshot dependencies
  (update-release-notes old-version new-version)
  (spit ".pallet-release" new-version)
  (println)
  (println (release-notes new-version))
  (println
   "\nCheck project.clj, ReleaseNotes and README (finish will commit these)"))

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

(defn wait
  [project args]
  (let [origin (git/origin)
        current-branch (git/current-branch)
        {:keys [login name]} (github/url->repo origin)]
    (println "Waiting for travis:" current-branch)
    (loop []
      (let [builds (travis-api/builds-for login name current-branch)]
        (if (and (seq builds)
                 (every? #(= "finished" (:state %)) builds))
          (let [r (apply max (map #(:result %) builds))]
            (when (pos? r)
              (throw (ex-info "Travis Build Failed"
                              {:exit-code r}))))
          (do
            (print (mapv :state builds))
            (flush)
            (Thread/sleep 10000)
            (recur)))))))

(defn finish
  "Finish a PalletOps release"
  [project args]
  (let [new-version (new-version)]
    (lein/clean project)
    (test project)
    (git/add "-u")
    (git/commit
     (str "Updated project.clj, release notes and readme for " new-version))
    (git/push "origin" (git/current-branch))
    (.delete (file ".pallet-release"))
    (println
     "Wait for travis to push to master,\n"
     "then run `lein pallet-release publish` to publish jars")
    (wait project args)))

(defn publish
  "Publish jars from master to clojars"
  [project args]
  (git/checkout "master")
  (git/pull)
  (lein/clean project)
  (lein/deploy project "clojars"))
