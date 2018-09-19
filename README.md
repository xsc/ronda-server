# ronda-server [![Build Status](https://travis-ci.org/xsc/ronda-server.svg?branch=master)](https://travis-ci.org/xsc/ronda-server)

A Clojure library to provide an [aleph][aleph]-backed HTTP server, exposing
[ronda][ronda]-enabled routes, endpoints and middlewares.

[aleph]: https://github.com/ztellman/aleph
[ronda]: https://github.com/xsc/ronda-routing

## Quickstart

```clojure
(require '[ronda.server :as server]
         '[ronda.routing.bidi :as bidi]
         '[com.stuartsierra.component :as component])
```

The server component is created by supplying a configuration, system component,
as well as route, endpoint and middleware constructors:

```clojure
(defonce server
  (server/make
    {:configuration {:ronda.server/httpd {:port 3000}
                     :db {:host "localhost"}}
     :system        (component/system-map
                      {:database (component/using
                                   (map->Database {})
                                   [:configuration])})
     :routes        make-routes
     :endpoints     make-endpoints
     :middlewares   make-middlewares}))

(alter-var-root #'server component/start)
```

Both `:configuration` and `:system` can be components, with the configuration
being injected into the system on startup.

Routes are defined using a ronda `RouteDescriptor`, e.g. via
[bidi][ronda-routing-bidi], and provided by a function that gets passed the
running `:system`:

[ronda-routing-bidi]: https://github.com/xsc/ronda-routing-bidi

```clojure
(defn make-routes
  [system]
  (bidi/descriptor
    ["/v1" {"/counter" :counter}]))
```

Same goes for endpoints:

```clojure
(defn make-endpoints
  [{:keys [database] :as system}]
  {:counter (fn [request]
             (db/count! database)
             {:status 204})})
```

Middlewares are provided by wrapping the prepared handler:

```clojure
(defn make-middlewares
  [handler system]
  (-> handler
      (wrap-json)
      (wrap-cors)))
```

## License

```
MIT License

Copyright (c) 2018 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
