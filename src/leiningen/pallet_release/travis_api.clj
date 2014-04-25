(ns leiningen.pallet-release.travis-api
  "API wrapper for travis"
  (:require
   [clj-http.client :as http]
   [cheshire.core :as json]
   [tentacles.core :as tentacles])
  (:import
   java.net.URLEncoder))

(def ^:dynamic *url* "https://api.travis-ci.org")

(defn format-url
  "Return a URL string. Called URLEncoder/encode on
   the elements of positional and then formats them in."
  [url path args]
  (str url (apply format path (map #(URLEncoder/encode (str %) "UTF-8") args))))

(defn make-request
  [method url path args
   {:keys [accept oauth-token etag user-agent if-modified-since
           throw-exceptions follow-redirects]
    :as options}]
  (->
   {:url (format-url url path args)
    :method method
    :insecure? true
    :throw-exceptions false
    :follow-redirects true}
   (merge (select-keys options [:throw-exceptions :follow-redirects]))
   (cond->
    accept (update-in [:headers] assoc "Accept" accept)
    etag (update-in [:headers] assoc "If-None-Match" etag)
    if-modified-since (update-in [:headers]
                                 assoc "If-Modified-Since" if-modified-since)
    oauth-token (update-in [:headers]
                           assoc "Authorization" (str "token " oauth-token))
    user-agent (update-in [:headers] assoc "User-Agent" user-agent))))

(defn parse-json
  "Same as json/parse-string but handles nil gracefully."
  [s] (when s (json/parse-string s true)))

(defn parse [{:keys [headers status body] :as response}]
  (clojure.pprint/pprint response)
  (flush)
  (if (#{400 401 204 422 403 404 500} status)
    (update-in response [:body] parse-json)
    (let [content-type (get headers "content-type")]
      (if (.contains content-type "application/json")
        (parse-json body)
        body))))

(defn api-call
  [method path args options]
  (let [request (make-request method *url* path args options)]
    (parse (http/request request))))

(defn history
  [org repo]
  (api-call :get "/repos/%s/%s/builds" [org repo] {}))

(defn build
  [id]
  (api-call :get "/builds/%s" [id] {}))

(defn log
  [id]
  (api-call :get "/jobs/%s/log" [id] {}))

(defn builds-for
  [org repo branch]
  (let [response (history org repo)]
    (if (map? response)
      response
      (filter #(= branch (:branch %)) response))))
