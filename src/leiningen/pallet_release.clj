(ns leiningen.pallet-release
  (:require
   [leiningen.pallet-release.core :refer [fail]]
   [leiningen.pallet-release.lein :as lein]
   [leiningen.pallet-release.core :refer [fail]]
   [leiningen.pallet-release.git :as git]
   [leiningen.pallet-release.release :as release]
   [leiningen.pallet-release.travis :as travis]))

(defn release [project [old-version new-version]]
  (when-not (and old-version new-version)
    (fail ":release requires old-version and new-version as arguments")))

(defn cmd-kw [cmd]
  {:pre [(or (nil? cmd) (string? cmd))]}
  (if (and cmd (.startsWith cmd ":"))
    (keyword (subs cmd 1))))

(defn init
  "Initialise the project for release via travis."
  [project [token]]
  (when-not (and token (= 40 (count token)))
    (fail "init expects a 40 character github token to be used to push."))
  (git/ensure-origin)
  (git/ensure-git-flow)
  (git/add-release-notes-md)
  (lein/init project)
  (travis/init project token)
  (println "Next:")
  (println "a) Commit .travis.yml, profiles.clj and ReleaseNotes.md")
  (println "b) Ensure that pbors has write access to the github repo"))

(defn pallet-release
  "Pallet release commands."
  ^{:subtasks
    [#'init #'release/start #'release/finish #'travis/push #'release/publish]}
  [project & [cmd & args]]
  (case cmd
    "init" (init project args)
    "start" (release/start project args)
    "finish" (release/finish project args)
    "push" (travis/push project args)
    "publish" (release/publish project args)
    (fail "Known cmds are init, start, finish, and push")))
