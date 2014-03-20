{:dev {:plugins [[lein-pallet-release "0.1.0-SNAPSHOT"]],
       :pallet-release {:url "https://pbors:${GH_TOKEN}@github.com/palletops/lein-pallet-release.git",
                        :branch "master"}},
 :no-checkouts {:checkout-deps-shares ^{:replace true} []},
 :release {:set-version
           {:updates
            [{:path "README.md",:no-snapshot true}
             {:path "src/leiningen/pallet_release/lein.clj"
              :search-regex
              #"lein-pallet-release \"\d+\.\d+\.\d+(-SNAPSHOT)?\""}]}}}
