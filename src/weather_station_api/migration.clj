(ns weather-station-api.migration
  (:require [clojure.java.jdbc :as sql]))

(defn migrated? [spec]
  (-> (sql/query spec
    [(str "select count(*) from information_schema.tables "
          "where table_name='measurements'")])
      first :count pos?))

(defn migrate [spec]
  (when (not (migrated? spec))
    (print "Creating database structure...") (flush)
    (sql/db-do-commands spec
      (sql/create-table-ddl :measurements
        [[:dt :timestamp]
         [:measurement_type "char(4)"]
         [:measurement_subtype "varchar(10)"]
         [:value :real]]))

(println " done")))
