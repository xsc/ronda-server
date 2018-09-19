(ns ronda.server.system.handler
  (:require [ronda.server.middlewares :as middlewares]
            [ronda.routing :as routing]
            [peripheral.core :as p]))

(defn compile-handler
  [{:keys [system routes endpoints middlewares]}]
  (-> (endpoints system)
      (routing/compile-endpoints)
      (middlewares system)
      (middlewares/wrap)
      (routing/wrap-routing (routes system))))

(p/defcomponent Handler [system
                         routes
                         endpoints
                         middlewares]
  :this/as *this*
  :handler (compile-handler *this*)

  clojure.lang.IFn
  (invoke [this request]
    (handler request)))

(defn make
  []
  (map->Handler {}))
