(ns lein-pallet-release.plugin
  (:require
   [clojure.string :refer [join split]]
   [leiningen.core.project :refer [add-profiles]]
   [com.palletops.leinout.git :as git]
   [com.palletops.leinout.github :as github]))

(defn src-uri
  [project]
  (try
    (if-let [origin (git/origin)]
      (let [branch (git/current-branch)
            {:keys [login name]} (github/url->repo origin)]
        (format "https://github.com/%s/%s/blob/%s"
                login name branch)))
    (catch Exception _)))               ; don't error on no git

(defn doc-version
  [project]
  (if-let [version (:version project)]
    (join "." (take 2 (split version #"\.")))
    "no-version"))

(defn profiles
  [project]
  (let [doc-v (doc-version project)]
    {:no-checkouts {:checkout-deps-shares ^:replace []}
     :release {:set-version
               {:updates [{:path "README.md" :no-snapshot true}]}}
     :doc-base {:dependencies '[[com.palletops/pallet-codox "0.1.0"]]
                :plugins '[[codox/codox.leiningen "0.6.4"]
                           [lein-marginalia "0.7.1"]]
                :codox {:writer 'codox-md.writer/write-docs
                        :output-dir (format "doc/%s/api" doc-v)
                        :src-dir-uri (src-uri project)
                        :src-linenum-anchor-prefix "L"}
                :aliases {"marginalia" ["marg"
                                        "-d" (format "doc/%s/annotated" doc-v)]
                          "codox" ["doc"]
                          "doc" ["do" "codox," "marginalia"]}}
     :doc [:no-checkouts :doc-base]}))

(defn middleware
  "Middleware to add profiles."
  [project]
  (-> project
      (add-profiles (profiles project))))
