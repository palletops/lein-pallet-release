{:dev
 {:pallet-release
  {:url "https://pbors:${GH_TOKEN}@github.com/palletops/lein-pallet-release.git",
   :branch "master"}},
 :no-checkouts {:checkout-deps-shares ^{:replace true} []},
 :release {:set-version
           {:updates
            [{:path "README.md",:no-snapshot true}
             {:path "src/leiningen/pallet_release/release.clj"
              :search-regex
              #"lein-pallet-release \"\d+\.\d+\.\d+(-SNAPSHOT)?\""}]}}}
