(ns leiningen.pallet-release
  "Pallet release commands to support the PalletOps release workflow."
  (:require
   [leiningen.pallet-release.core :refer [fail]]
   [leiningen.pallet-release.release :as release]
   [leiningen.pallet-release.travis :as travis]))

(defn pallet-release
  "Pallet release commands to support the PalletOps release workflow."
  ^{:subtasks
    [#'release/init #'release/start #'release/finish
     #'travis/push #'release/publish]}
  [project & [cmd & args]]
  (case cmd
    "init" (release/init project args)
    "start" (release/start project args)
    "finish" (release/finish project args)
    "push" (travis/push project args)
    "publish" (release/publish project args)
    (fail "Known cmds are init, start, finish, and push")))
