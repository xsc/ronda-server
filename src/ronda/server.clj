(ns ronda.server
  (:require [ronda.server
             [system :as system]
             [utils :as utils]]
            [peripheral.core :as p]))

(defn make
  "Create a system that connects components according to the given spec and
   exposes an HTTP interface.

   - `:system`: a component system to inject into the handler,
   - `:configuration`: a configuration component to inject into the system,
   - `:routes`: a function that takes the running system and produces a ronda
     route descriptor,
   - `:endpoints`: a function that takes the running system and produces a ronda
     endpoint map,
   - `:middlewares`: a function that takes a Ring handler and the running system
     and attaches middlewares to the handler.

   The configuration can be used to provide HTTP server settings using
   a `:ronda.server/httpd` key. Please refer to aleph's `start-server`
   documentation to see which options can be used.

   The server's default port is 3000."
  [spec]
  (system/make spec))

(defn make-handler
  "Create the full, wrapped handler based on the given spec (see [[make]])."
  [spec]
  (utils/map->HandlerOnly
    {:system (make spec)}))

(defn run
  "See [[make]]. Will directly start up the component."
  [spec]
  (p/start (make spec)))

(defn port
  "Get the port of a _running_ server created with [[make]]."
  [server]
  (-> server :httpd :port))
