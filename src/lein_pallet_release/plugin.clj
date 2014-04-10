(ns lein-pallet-release.plugin
  (:require
   [leiningen.core.project :refer [add-profiles]]))

(def profiles
  {:no-checkouts {:checkout-shares ^:replace []}
   :release {:set-version
             {:updates [{:path "README.md" :no-snapshot true}]}}})

(defn middleware
  "Middleware to add profiles."
  [project]
  (-> project
      (add-profiles profiles)))
