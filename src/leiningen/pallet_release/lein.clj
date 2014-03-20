(ns leiningen.pallet-release.lein
  "Leiningen configuration for palletops release process"
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :refer [file]]
   [fipp.edn :refer [pprint]]
   [leiningen.core.main :refer [apply-task debug info]]
   [leiningen.pallet-release.core :refer [release-config]])
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

(defn release-profiles [project]
  {:dev {:plugins '[[lein-pallet-release "0.1.0"]]
         :pallet-release (release-config project)}
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

(defn clean
  [project]
  (apply-task "clean" project []))

(defn test
  [project]
  (apply-task "with-profile" project ["+no-checkouts" "test"]))

(defn update-versions
  [project old-version new-version]
  (apply-task "with-profile" project
              ["+release" "set-version"
               new-version ":previous-version" old-version]))

(defn update-versions
  [project old-version new-version]
  (apply-task "with-profile" project
              ["+release" "set-version"
               new-version ":previous-version" old-version]))

(defn set-next-version
  [project]
  (apply-task "with-profile" project ["+release" "set-version" ":point"]))

(defn deploy
  [project]
  (apply-task "deploy" project ["clojars"]))
