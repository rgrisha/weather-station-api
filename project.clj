(defproject weather-station-api "0.0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [org.postgresql/postgresql "42.1.4"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [compojure "1.6.0"]
                 [ring/ring-json "0.4.0"]
                 [clj-time "0.14.2"]]
  :plugins [[lein-ring "0.12.3"]]
  :profiles {:user {:plugins  [[venantius/ultra "0.5.2"]]}
             :uberjar  {:aot :all}}
  :test-paths  ["test"]
  :main weather-station-api.web
  ; :ring {:handler weather-station-api.web/app}
  :uberjar-name "weather-station-api.jar"
  )
