(ns ronda.server-test
  (:require [ronda.server :as server]
            [ronda.routing.bidi :as bidi]
            [peripheral.core :as p]
            [aleph.http :as http]
            [clojure.test :refer :all]))

;; ## Fixtures

(defn routes
  [system]
  (bidi/descriptor
    ["/" {"200" :200
          "204" :204
          "from-configuration" :cfg
          "from-system" :sys}]))

(defn endpoints
  [{:keys [configuration other-handler]}]
  {:200 (constantly {:status 200})
   :204 (constantly {:status 204})
   :cfg (constantly {:status (:endpoint-result configuration)})
   :sys other-handler})

(defn middlewares
  [handler system]
  (fn [{:keys [uri] :as request}]
    (if (= uri "/call-middleware")
      {:status (-> system :configuration :middleware-result)}
      (handler request))))

(def configuration
  {:ronda.server/httpd {:port 0}
   :endpoint-result    201
   :middleware-result  400})

(def system,
  {:other-handler (constantly {:status 202})})

(def spec
  {:routes        routes
   :endpoints     endpoints
   :middlewares   middlewares
   :system        system
   :configuration configuration})

;; ## Helpers

(defn status-fetcher
  [base]
  (fn [path]
    (-> (http/get (str base path) {:throw-exceptions? false})
        (deref)
        (:status))))

;; ## Tests

(deftest t-server
  (p/with-start [server (server/make spec)]
    (let [fetch (status-fetcher
                  (format "http://localhost:%d" (server/port server)))]
      (is (= 200 (fetch "/200")))
      (is (= 204 (fetch "/204")))
      (is (= 201 (fetch "/from-configuration")))
      (is (= 202 (fetch "/from-system")))
      (is (= 400 (fetch "/call-middleware")))
      (is (= 404 (fetch "/unknown"))))))
