(ns leiningen.pallet-release.core)

(defn deep-merge
  "Recursively merge maps."
  [& ms]
  (letfn [(f [a b]
            (if (and (map? a) (map? b))
              (deep-merge a b)
              b))]
    (apply merge-with f ms)))

(defn fail
  "Fail with the given message, msg."
  [msg]
  (throw (ex-info msg {:exit-code 1})))

(defn fail-on-error
  "Fail on a shell error"
  [exit]
  (when (and exit (pos? exit))
    (fail "Shell command failed")))


(def push-repo-fmt
  "https://pbors:${GH_TOKEN}@github.com%s.git")

(defn repo-coordinates
  [{:keys [url] :as project}]
  (when-not (or url (-> project :pallet-release :url))
    (fail "No :url available in project.clj"))
  (if-let [release-url (-> project :pallet-release :url)]
    release-url
    (let [u (java.net.URL. url)]
      (if (= "github.com" (.getHost u))
        (format push-repo-fmt (.getPath u))
        (fail (str "Don't know how to create a pushable git url from "
                   url))))))

(defn release-config
  "Return a pallet release configuration map"
  [project]
  {:url (repo-coordinates project)
   :branch (or (-> project :pallet-release :branch)
               "master")})
