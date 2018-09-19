(ns ronda.server.system
  (:require [ronda.server.system
             [handler :as handler]
             [httpd :as httpd]]
            [peripheral.core :as p]))

(p/defsystem+ ServerSystem [routes endpoints middlewares]
  :configuration []
  :system        [:configuration]
  :handler       [:system :routes :endpoints :middlewares]
  :httpd         [:configuration :handler])

(defn make
  [{:keys [system routes endpoints middlewares configuration]
    :or {system        {}
         configuration {}
         middlewares   (fn [handler system] handler)}}]
  {:pre [(some? routes) (some? endpoints)]}
  (map->ServerSystem
    {:configuration configuration
     :system        system
     :middlewares   middlewares
     :routes        routes
     :endpoints     endpoints
     :handler       (handler/make)
     :httpd         (httpd/make)}))
