(ns weather-station-api.web
  (:require [weather-station-api.model :as model]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as mw-json]
            [ring.adapter.jetty :as ring]
            [ring.logger :as logger]
            [ring.util.response :as response])
  (:gen-class))

(defroutes app-routes
  (GET "/measurements" request
      {:status 200
       :body (model/get-measurements)})
  (POST "/measurement" {body :body}
      (println (str "got json body" body))  
      (model/add-measurement body)  
      {:status 201})
  (route/not-found {:status 404 :body "Not Found"}))

(def app
  (-> (handler/site app-routes)
      (mw-json/wrap-json-body {:keywords? true})
      mw-json/wrap-json-response))

(defn start  [port]
  (ring/run-jetty 
    (logger/wrap-with-logger app) 
    {:port port :join? false}))

(defn -main  []
  (model/migrate)
  (let  [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))
