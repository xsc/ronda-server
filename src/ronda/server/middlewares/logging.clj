(ns ronda.server.middlewares.logging
  (:require [ronda.routing :as routing]
            [clojure.tools.logging :as log]
            [cheshire.core :as json])
  (:import [org.slf4j MDC]))

;; ## License

;; Copyright 2016 stylefruits GmbH
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

;; ## Helpers

(defn- nano-time
  []
  (System/nanoTime))

(defn- context->mdc-map
  [{:keys [headers scheme request-method uri query-string pools]}
   {:keys [status]}
   execution-time]
  {"user-agent"     (get headers "user-agent")
   "schema"         (some-> scheme name)
   "request-method" (some-> request-method name)
   "uri"            uri
   "query-string"   query-string
   "content-length" (get headers "content-length")
   "status"         (str status)
   "execution-time" (str execution-time)})

(defn- with-mdc*
  [context f]
  (doseq [[name value] context]
    (MDC/put (str name) (str value)))
  (try
    (f)
    (finally
      (doseq [[name _] context]
        (MDC/remove (str name))))))

;; ## Middleware

(defn- response-or-500
  [handler request]
  (try
    (or (handler request)
        {:status 404})
    (catch Throwable t
      (->> (dissoc request :ronda/routing :ronda/descriptor)
           (pr-str)
           (log/errorf t "an exception occured when processing:%n%s"))
      {:status 500
       :body (json/generate-string
               {:error (format "[%s] %s"
                               (.getName (class t))
                               (.getMessage t))})})))

(defn- format-for-logging
  [{:keys [request-method uri query-string]
    :or {request-method "NO_METHOD"
         uri            "NO_URI"}
    :as request}
   {:keys [status] :or {status 404}}
   delta]
  (let [endpoint-id (or (routing/endpoint request) :unknown)
        method (.toUpperCase (name request-method))]
    (format "[ring] [%s] [%s] %s %s%s [%.3fs]"
            (name endpoint-id)
            status
            method
            uri
            (if query-string
              (str "?" query-string)
              "")
            delta)))

(defn wrap-logging
  [handler]
  (fn [request]
    (let [start       (nano-time)
          response    (response-or-500 handler request)
          delta       (/ (quot (- (nano-time) start) 1e6) 1e3)
          endpoint-id (or (routing/endpoint request) :unknown)]
      (with-mdc*
        (context->mdc-map request response delta)
        #(log/info (format-for-logging request response delta)))
      response)))
