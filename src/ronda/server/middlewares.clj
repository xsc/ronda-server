(ns ronda.server.middlewares
  (:require [ronda.server.middlewares
             [logging :refer [wrap-logging]]]))

(defn wrap
  [handler]
  (-> handler
      (wrap-logging)))
