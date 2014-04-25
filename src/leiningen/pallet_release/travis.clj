(ns leiningen.pallet-release.travis
  (:require
   [clojure.string :as string :refer [blank?]]
   [clojure.java.io :refer [file resource]]
   [leiningen.core.eval :as eval]
   [leiningen.core.main :refer [debug info]]
   [leiningen.pallet-release.core
    :refer [fail fail-on-error release-notes]]
   [leiningen.pallet-release.git :as git]
   [leiningen.pallet-release.github :as github]
   [leiningen.pallet-release.lein :as lein])
  (:import
   java.io.File))

(defn ^File travis-yml-file
  "Return the travis.yml file."
  [project]
  {:pre [(map? project) (:root project)]}
  (file (:root project) ".travis.yml"))

(defn create-travis-yml
  "Write the .travis.yml file."
  [project ^File f]
  {:pre [(map? project) f (not (.exists f))]}
  (debug "Push repo coords" (lein/release-repo-coordinates project))
  (spit f
        (format
         (slurp (resource "leiningen/pallet_release/travis.yml"))
         (lein/release-repo-coordinates project))))

(defn enable
  "Enable travis on the project."
  [project]
  (fail-on-error (eval/sh "travis" "enable")))

(defn init
  "Initialise the project for travis."
  [project token]
  (enable project)
  (let [f (travis-yml-file project)]
    (when-not (.exists f)
      (info "Writing" (.getPath f))
      (create-travis-yml project f)
      (fail-on-error
       (eval/sh "travis" "encrypt" (str "GH_TOKEN=\"" token "\"") "--add")))
    (fail-on-error (eval/sh "git" "add" ".travis.yml" "profiles.clj"))))

(defn filter-string
  [s secret]
  (string/replace s secret "****"))

(defmacro with-out-err-str
  "Execute body with out and err redirected to a StringWriter
  instance.  Return the concatenation of the two writers as a
  string."
  [& body]
  `(let [o# (new java.io.StringWriter)
         e# (new java.io.StringWriter)]
     (binding [*err* e#
               *out* o#]
       ~@body)
     (println "out " (str o#))
     (println "err " (str e#))
     (str o# e#)))

(defmacro print-filtered [secret & body]
  `(let [s# (new java.io.StringWriter)
         out# *out*]
     (binding [*out* s# *err* s#]
       (try
         ~@body
         (finally
           (binding [*out* out#]
             (println (filter-string (str s#) ~secret))))))))

(defn do-push
  "Push current HEAD to url branch, substituting values from env in url."
  [project {:keys [url branch]} gh-token]
  {:pre [url branch]}
  (let [env {:GH_TOKEN gh-token}
        url (reduce-kv #(string/replace % (str "${" (name %2) "}") %3) url env)
        sha (git/current-sha)
        release-branch (git/current-branch)
        tag (string/replace release-branch "release/" "")
        merge-msg (str "Merge " release-branch)]
    (info "Tagging" release-branch "with" tag)
    (if-not (blank? (System/getenv "PALLET_SHOW_CREDENTIALS"))
      (debug "push to url" url))

    (print-filtered gh-token (git/add-remote "github" url))
    (git/config {"user.email" "hugo@palletops.com"
                 "user.name" "Hugo Duncan"})

    (git/tag "-a" "-m" (str "Release " tag) "-m" (release-notes tag) tag)

    ;; merge to master
    (info "Merging" release-branch "to" branch)
    (git/fetch "origin" branch)
    (git/checkout "-b" branch "FETCH_HEAD")
    (git/merge "-m" merge-msg tag)
    (print-filtered gh-token (git/push "github" (str "HEAD:" branch)))
    (print-filtered gh-token (git/push "github" "--tags"))

    ;; push to develop
    (info "Pushing" release-branch "to" "develop")
    (git/checkout tag)
    (lein/set-next-version project)
    (git/add "-u")
    (git/commit "Updated version for next release cycle")
    (print-filtered gh-token (git/push "github" "HEAD:develop"))))

(defn push
  "Push a PalletOps project from travis"
  [project args]
  (let [branch (git/current-branch)]
    (info "Building branch" branch)
    (lein/test project)
    ;; (lein/check project) comment as it seems to exit the process
    (when (.startsWith branch "release/")
      (info "Processing release")
      (let [origin (git/origin)
            coords (merge
                    {:branch "master" :url (github/repo-coordinates origin)}
                    (:pallet-release project))]
        (when-not (every? coords [:url :branch])
          (fail
           "project.clj fails to specify :url and :branch in :pallet-release"))
        (let [gh-token (System/getenv "GH_TOKEN")]
          (do-push project coords gh-token))))
    (info "Push complete")))
