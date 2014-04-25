(ns leiningen.pallet-release
  "Pallet release commands to support the PalletOps release workflow."
  (:require
   [clojure.java.io :refer [input-stream resource]]
   [leiningen.pallet-release.core :refer [fail]]
   [leiningen.pallet-release.release :as release]
   [leiningen.pallet-release.travis :as travis]))

(def prop-file
  "META-INF/maven/lein-pallet-release/lein-pallet-release/pom.properties")

(defn version []
  (let [props (doto (java.util.Properties.)
                (.load (input-stream (resource prop-file))))]
    (println "lein-pallet-release" (get props "version"))))

(defn pallet-release
  "Pallet release commands to support the PalletOps release workflow."
  ^{:subtasks
    [#'release/init #'release/start #'release/finish
     #'travis/push #'release/publish]}
  [project & [cmd & args]]
  (case cmd
    "init" (release/init project args)
    "auth" (release/auth project args)
    "start" (release/start project args)
    "finish" (release/finish project args)
    "push" (travis/push project args)
    "wait" (release/wait project args)
    "publish" (release/publish project args)
    "version" (version)
    (fail "Known cmds are init, auth, start, finish, and publish.")))
