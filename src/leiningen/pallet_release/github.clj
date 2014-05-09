(ns leiningen.pallet-release.github
  "Github interaction"
  (:require
   [com.palletops.leinout.github :as github]
   [leiningen.core.eval :as eval]
   [tentacles.orgs :as orgs]))

(defn auth-builder
  "Ensure the build-automation team is authorised on the repository."
  [url token]
  (let [{:keys [login name] :as repo} (github/url->repo url token)]
    (if-let [builder-id (github/team-id "build-automation" login token)]
      (let [r (github/auth-team-id builder-id repo token)]
        (case  r
          :authorised (println "Authourised")
          :already-authorised (println "Already authorised")
          :authorisation-failed (println "Authorisation failed")))
      (println
       "No team found for" (str login "/build-automation.")
       "Either create the build-automation team, or authorise pbors."))))

(def push-repo-fmt
  "https://pbors:${GH_TOKEN}@github.com/%s/%s.git")

(defn repo-coordinates
  [origin]
  (let [{:keys [login name]} (github/url->repo origin)]
    (format push-repo-fmt login name)))
