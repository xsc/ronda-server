(ns ronda.server.utils
  (:require [peripheral.core :as p]))

(p/defcomponent HandlerOnly [system]
  :component/inner (p/subsystem system [:handler])
  :handler         (:handler inner)

  clojure.lang.IFn
  (invoke [this request]
    (handler request)))
