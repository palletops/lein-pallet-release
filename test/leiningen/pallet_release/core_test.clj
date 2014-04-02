(ns leiningen.pallet-release.core-test
  (:require
   [clojure.test :refer :all]
   [leiningen.pallet-release.core :refer :all]))

(deftest release-notes-test
  (is (= "- Initial release" (release-notes "0.1.0"))))
