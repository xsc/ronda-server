(ns ronda.server.system.httpd
  (:require [peripheral.core :refer [defcomponent]]
            [clojure.tools.logging :refer [infof]]
            [aleph.http :as aleph]))

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

;; ## Options

(def ^:private default-opts
  {:bind "0"
   :port 3000})

(defn- read-opts
  [{:keys [ronda.server/httpd]}]
  (merge default-opts httpd))

;; ## Start/Stop

(defn- start-server!
  [handler opts]
  (aleph/start-server handler opts))

(defn- stop-server!
  [^java.io.Closeable server]
  (.close server)
  (aleph.netty/wait-for-close server))

(defn- print-startup-message!
  [{:keys [port opts]}]
  (infof "server is running on %s:%d ..." (:bind opts) port))

;; ## Component

(defcomponent Httpd [configuration handler]
  :this/as *this*
  :opts
  (read-opts configuration)

  :keep-alive-promise
  (promise)
  #(deliver % true)

  :srv
  (start-server! handler opts)
  stop-server!

  :port
  (aleph.netty/port srv)

  :keep-alive-thread
  (doto (Thread. #(deref keep-alive-promise))
    (.start))

  :on/started (print-startup-message! *this*))

(defn make
  []
  (map->Httpd {}))
