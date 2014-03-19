(ns leiningen.pallet-release.lein
  "Leiningen configuration for palletops release process"
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [file]]
   [fipp.edn :refer [pprint]]
   [leiningen.core.main :refer [debug info]]
   [leiningen.pallet-release.travis :as travis])
  (:import
   java.io.File))

(defn deep-merge
  "Recursively merge maps."
  [& ms]
  (letfn [(f [a b]
            (if (and (map? a) (map? b))
              (deep-merge a b)
              b))]
    (apply merge-with f ms)))

(defn ensure-alias
  "Ensure a release alias in project.clj"
  [])

(defn ensure-plugins
  "Ensure a release alias in project.clj"
  [])

(defn release-profiles [project]
  {:dev {:plugins '[[lein-pallet-release "0.1.0-SNAPSHOT"]]
         :pallet-release (travis/release-config project)}
   :no-checkouts {:checkout-deps-shares ^:replace []}
   :release {:set-version
             {:updates [{:path "README.md" :no-snapshot true}]}}})

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

(defn init
  "Initialise project for release"
  [project]
  (let [profiles (deep-merge
                  (release-profiles project)
                  (read-profiles (profiles-clj-file project)))]
    (info "Writing" (.getPath (profiles-clj-file project)))
    (spit (profiles-clj-file project)
          (binding [*print-meta* true]
            (with-out-str (pprint (release-profiles project)))))))
