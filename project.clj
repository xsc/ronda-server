(defproject ronda/server "0.1.0"
  :description "HTTP server wrapper around ronda routes and components"
  :url "https://github.com/xsc/ronda-server"
  :license {:name "MIT License"
            :url "none"
            :year 2018
            :key "mit"}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]

                 ;; logging dependencies
                 [org.clojure/tools.logging "0.4.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.slf4j/slf4j-api "1.7.25"]
                 [fipp "0.6.12"]

                 ;; http dependencies
                 [aleph "0.4.6"]
                 [ronda/routing "0.2.8"]
                 [cheshire "5.8.0"]

                 ;; component dependencies
                 [peripheral "0.5.3"]]
  :profiles {:dev {:dependencies [[ronda/routing-bidi "0.1.3"]
                                  [bidi "2.1.4"]]}}
  :pedantic? :abort)
