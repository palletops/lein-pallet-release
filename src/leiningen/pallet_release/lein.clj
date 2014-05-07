(ns leiningen.pallet-release.lein
  "Leiningen configuration for palletops release process."
  (:refer-clojure :exclude [test])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [file]]
   [com.palletops.leinout.lein :refer [task]]
   [leiningen.core.main :refer [apply-task debug info *exit-process?*]])
  (:import
   java.io.File))

(defn release-repo-coordinates [project]
  (-> project :pallet-release :url))

(defn test
  [project]
  (debug "lein test")
  (task project "with-profile" "+no-checkouts" "test"))

(defn update-versions
  [project old-version new-version]
  (debug "lein set-version" new-version)
  (task project "with-profile"
        "+release" "set-version"
        new-version ":previous-version" old-version))

(defn set-next-version
  [project]
  (debug "lein set-version :point")
  (task project "with-profile" "+release" "set-version" ":point"))
