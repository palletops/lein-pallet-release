(defproject lein-pallet-release "0.1.10-SNAPSHOT"
  :description "A leiningen plugin for the PalletOps release workflow"
  :url "http://github.com/palletops/lein-pallet-release"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[lein-set-version "0.4.1"]
                 [org.clojars.hugoduncan/fipp "0.4.1.1"]
                 [tentacles "0.2.5"]]
  :eval-in-leiningen true)
