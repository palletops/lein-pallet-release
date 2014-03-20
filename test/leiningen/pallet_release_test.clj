(ns leiningen.pallet-release-test
  (:require
   [clojure.test :refer :all]
   [leiningen.pallet-release :refer :all]))

(deftest pallet-release-test
  (is (thrown? clojure.lang.ExceptionInfo
              (pallet-release "unknown"))))
