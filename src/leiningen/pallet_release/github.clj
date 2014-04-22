(ns leiningen.pallet-release.github
  "Github interaction"
  (:require
   [leiningen.core.eval :as eval]
   [tentacles.orgs :as orgs]))

(defn github-login
  "Return a github login key from .authinfo"
  []
  (let [text (with-out-str
               (eval/sh
                "/usr/local/bin/gpg2" "-q" "--no-tty" "-d"
                (str (System/getProperty "user.home") "/.authinfo.gpg")))
        m (re-find #"machine github.com login ([^ \n]*).*" text)]
    (second m)))

(defn builder-team
  "Return the id of the build-automation team for org, given `token`."
  [org token]
  (->> (orgs/teams org {:auth token})
       (filter #(= "build-automation" (:slug %)))
       first
       :id))

(defn team-repos
  [id token]
  (orgs/list-team-repos id {:auth token}))

(defn repo-matching
  "Return the repo matching the git or https url for the repo."
  [repos url]
  (filterv #((set ((juxt :ssh_url :clone_url) %)) url) repos))

(defn url->repo
  "Return a partial repository map from a url"
  [url]
  (let [x (or (re-matches #"git@github.com:([^/]+)/(.+).git" url)
              (re-matches #"git://github.com/([^/]+)/(.+).git" url)
              (re-matches #"https://github.com/([^/]+)/(.+).git" url))]
    (cond-> {:login (second x)
             :name (last x)}
            (.startsWith url "git@") (assoc :ssh_url url)
            (.startsWith url "git:") (assoc :git_url url)
            (.startsWith url "https") (assoc :clone_url url))))

(defn auth-builder
  "Ensure the build-automation team is authorised on the repository."
  [url token]
  (let [{:keys [login name]} (url->repo url)
        builder-id (builder-team login token)
        repos (team-repos builder-id token)]
    (if builder-id
      (when-not (repo-matching repos url)
        (orgs/add-team-repo builder-id login name {:auth token}))
      (println
       "No team found for" (str login "/build-automation.")
       "Either create the build-automation team, or authorise pbors."))))
