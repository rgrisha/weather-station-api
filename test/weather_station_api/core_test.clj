(ns weather-station-api.core-test
  (:require [clojure.test :refer :all]
            [weather-station-api.model :as model]))

(deftest model

  (is (= ["dt" "HUMI" "H" 1.5]
         (model/insert-row-vec 
           {:hum-t "11.11" :hum-h "1.5" :pres-t "33.33" :pres-p "44.44"}
           "dt"
           {:measurement :hum-h :db-data ["HUMI" "H"]})))

  (is (= [["dt" "TEMP" "H" 1.5]
          ["dt" "HUMI" "H" 2.5]
          ["dt" "TEMP" "P" 3.5]
          ["dt" "PRES" "P" 4.5]]
         (model/calc-ins-rows
            {:hum-t "1.5" :hum-h "2.5" :pres-t "3.5" :pres-p "4.5"}
            "dt")
         
       ))
  )
