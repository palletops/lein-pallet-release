(ns leiningen.pallet-release.lein
  "Leiningen configuration for palletops release process"
  (:refer-clojure :exclude [test])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [file]]
   [leiningen.core.main :refer [apply-task debug]])
  (:import
   java.io.File))

(defn ^File profiles-clj-file
  [{:keys [root] :as project}]
  {:pre [root]}
  (file root "profiles.clj"))

(defn read-profiles
  "Returns the profiles in the project profiles.clj"
  [^File f]
  {:pre [f]}
  (if (.exists f)
    (edn/read-string (slurp f))))

(defn clean
  [project]
  (debug "lein clean")
  (apply-task "clean" project []))

(defn test
  [project]
  (debug "lein test")
  (apply-task "with-profile" project ["+no-checkouts" "test"]))

(defn update-versions
  [project old-version new-version]
  (debug "lein set-version" new-version)
  (apply-task "with-profile" project
              ["+release" "set-version"
               new-version ":previous-version" old-version]))

(defn set-next-version
  [project]
  (debug "lein set-version :point")
  (apply-task "with-profile" project ["+release" "set-version" ":point"]))

(defn deploy
  [project]
  (debug "lein deploy")
  (apply-task "deploy" project ["clojars"]))
