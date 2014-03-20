(ns leiningen.pallet-release.git
  (:refer-clojure :exclude [merge])
  (:require
   [clojure.string :as string :refer [blank? trim]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [debug]]
   [leiningen.pallet-release.core :refer [fail fail-on-error]]))

(defn ensure-origin
  []
  (when (pos? (eval/sh "git" "remote" "show" "origin"))
    (fail "No origin repository.  Have you created it on github?")))

(defn ensure-git-flow
  []
  (let [m (with-out-str
            (eval/sh "git" "config" "--get" "gitflow.branch.master"))]
    (when (blank? m)
      (fail-on-error (eval/sh "git" "flow" "init" "-d")))))

(defn release-start
  [version]
  (debug "git flow release start" version)
  (fail-on-error (eval/sh "git" "flow" "release" "start" version)))

(defn add-remote
  [remote url]
  (debug "git remote add" remote url)
  (fail-on-error (eval/sh "git" "remote" "add" remote url)))

(defn tag
  [& args]
  (apply debug "git tag" args)
  (fail-on-error (apply eval/sh "git" "tag" args)))

(defn add
  [& args]
  (apply debug "git add" args)
  (fail-on-error (apply eval/sh "git" "add" args)))

(defn commit
  [msg]
  (debug "git commit -m" "\"" msg "\"")
  (fail-on-error (eval/sh "git" "commit" "-m" msg)))

(defn push
  [remote branch-spec]
  (debug "git push" remote branch-spec)
  (fail-on-error (eval/sh "git" "push" remote branch-spec)))

(defn checkout
  [& args]
  (apply debug "git checkout" args)
  (fail-on-error (apply eval/sh "git" "checkout" args)))

(defn pull
  []
  (debug "git pull")
  (fail-on-error (eval/sh "git" "pull")))

(defn fetch
  [& args]
  (apply debug "git fetch" args)
  (fail-on-error (apply eval/sh "git" "fetch" args)))

(defn merge
  [& args]
  (apply debug "git merge" args)
  (fail-on-error (apply eval/sh "git" "merge" args)))

(defn current-sha
  []
  (debug "git rev-parse HEAD")
  (trim (with-out-str (fail-on-error
                       (eval/sh "git" "rev-parse" "HEAD")))))

(defn current-branch
  []
  (debug "git current-branch")
  (let [sha (current-sha)
        out (with-out-str (fail-on-error
                           (eval/sh "git" "branch" "-v" "--no-abbrev")))
        line (->> out
                  (string/split-lines)
                  (filter #(.contains % sha))
                  (remove #(.contains % "detached"))
                  first
                  )]
    (->> (string/split (string/replace line "*" "") #" +")
         (remove blank?)
         first)))

(defn config
  [kvs]
  (debug "git config --global" (pr-str kvs))
  (doseq [[k v] kvs]
    (fail-on-error (eval/sh "git" "config" "--global" (name k) v))))
