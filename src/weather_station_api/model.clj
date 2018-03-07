(ns weather-station-api.model
   (:require [weather-station-api.migration :as migration]
             [clojure.java.jdbc :as sql]
             [clj-time.core :as t]
             [clj-time.jdbc]))


(defn spec-fn []

    (def db-uri 
      (java.net.URI. (System/getenv "DATABASE_URL")))

    (def user-and-password
      (if (nil? (.getUserInfo db-uri))
           nil 
           (clojure.string/split  (.getUserInfo db-uri) #":")))
    
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     ; :ssl true
     :sslfactory "org.postgresql.ssl.NonValidatingFactory"
     :user  (get user-and-password 0)
     :password  (get user-and-password 1)
     :subname  (if  (= -1  (.getPort db-uri))
                 (format "//%s%s"  (.getHost db-uri)  (.getPath db-uri))
                 (format "//%s:%s%s"  (.getHost db-uri)  (.getPort db-uri)  (.getPath db-uri)))})


(def sql-get-measurements
  (str "select hour, cast(avg(temp) as numeric(8,2)) as temp, "
        "   cast(avg(pres) as numeric(8,2)) as pressure, cast(avg(humi) as numeric(8,2)) as humidity "
        " from ( "
        "   select to_char(dt, 'YYYY-MM-DD HH24') as day, to_char(dt, 'HH24') as hour, value as temp, cast(null as real) as pres, cast(null as real) as humi "
        "   from measurements where measurement_type='TEMP' and age(now(), dt) < '1 day' "
        "   union all "
        "   select to_char(dt, 'YYYY-MM-DD HH24') as day, to_char(dt, 'HH24'), null, value, null "
        "   from measurements where measurement_type='PRES' and age(now(), dt) < '1 day' "
        "   union all "
        "   select to_char(dt, 'YYYY-MM-DD HH24') as day, to_char(dt, 'HH24'), null, null, value "
        "   from measurements where measurement_type='HUMI' and age(now(), dt) < '1 day' "
        " ) as m group by m.day, m.hour order by day; "
       ))

(def sql-get-last-measurements
  (str  "select to_char(dt, 'YYYY-MM-DD HH24:MI:SS') as dt, " 
        "   cast(avg(temp) as numeric(8,2)) as temp, "
        "   cast(avg(pres) as numeric(8,2)) as pressure, "
        "   cast(avg(humi) as numeric(8,2)) as humidity "
        " from ( "
        "   select dt, value as temp, cast(null as real) as pres, cast(null as real) as humi "
        "   from measurements where measurement_type='TEMP' and dt = (select max(dt) from measurements)"
        "   union all "
        "   select dt, null, value, null "
        "   from measurements where measurement_type='PRES' and dt = (select max(dt) from measurements)"
        "   union all "
        "   select dt, null, null, value "
        "   from measurements where measurement_type='HUMI' and dt = (select max(dt) from measurements)"
        " ) as m group by dt; " 
   ))

(defn get-measurements  []
  (def spec (spec-fn))
  (let [qres (sql/query spec sql-get-measurements)
        res (into [] qres)] 
    res))

(defn get-last-measurements  []
  (def spec (spec-fn))
  (let [qres (sql/query spec sql-get-last-measurements)
        res (into [] qres)
        dt (:dt (first res))] 
    {:dt dt
     :measurements (map (fn [row] (dissoc row :dt)) res)}))


(defn insert-row-vec [json-body dt m]
  [dt 
   (first (:db-data m))
   (second (:db-data m)) 
   (try 
     (print (str "body->>>>>" json-body))
     (Float/parseFloat ((:measurement m) json-body))
     (catch Exception e (println (str "error parsing measurement " (:measurement m) e json-body)) nil))])
 
(def pdata
  [{:measurement :hum-t :db-data ["TEMP" "H"]}
   {:measurement :hum-h :db-data ["HUMI" "H"]} 
   {:measurement :pres-t :db-data ["TEMP" "P"]}  
   {:measurement :pres-p :db-data ["PRES" "P"]}])

(defn calc-ins-rows [json-body dt]
    (->> pdata
         (map (partial insert-row-vec json-body dt))
         (filter (complement nil?))))

(defn add-measurement [json-body]
  (def spec (spec-fn))
  (println (str "got body " json-body))

  (def ins-rows (calc-ins-rows json-body (t/now)))

  (println ins-rows)

  (sql/insert-multi! spec :measurements
          nil ; column names omitted
          ins-rows))

(defn migrate []
  (def spec (spec-fn))
  (migration/migrate spec))
