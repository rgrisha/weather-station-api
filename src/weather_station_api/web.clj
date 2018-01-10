(ns weather-station-api.web
  (:require [weather-station-api.model :as model]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as mw-json]
            [ring.adapter.jetty :as ring]
            [ring.util.response :as response])
  (:gen-class))

(defroutes app-routes
  (POST "/" request
    (let [name (or (get-in request [:params :name])
                   (get-in request [:body :name])
                   "John Doe")]
      {:status 200
       :body {:name name
       :desc (str "The name you sent to me was " name)}}))
  (GET "/measurements" request
      {:status 200
       :body (model/get-measurements)})
  (POST "/measurement" {body :body}
      (model/add-measurement body)  
      {:status 201})
  (route/not-found {:status 404 :body "Not Found"}))

(def app
  (-> (handler/site app-routes)
      (mw-json/wrap-json-body {:keywords? true})
      mw-json/wrap-json-response))

(defn start  [port]
  (ring/run-jetty app {:port port
                       :join? false}))

(defn -main  []
  (let  [port (Integer. (or (System/getenv "PORT") "8080"))]
    (start port)))
