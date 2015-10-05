(ns event-site.component.db
  (:require [com.stuartsierra.component :as component]
            [monger.core :as mg])
  (:import [com.mongodb WriteConcern]
           (java.util.logging Level Logger)))

(defn stop [component]
  (mg/disconnect (get-in component [:mongo :conn]))
  (let [result (-> component
                   (assoc :mongo nil)
                   (assoc :conn nil))]
    result))

(defn silence-mongo-logging []
  (.setLevel (Logger/getLogger "org.mongodb") Level/WARNING))

(defrecord Db [uri]
  component/Lifecycle
  (start [this]
    (silence-mongo-logging)
    (if (:mongo this)
      this
      (let [mongo (mg/connect-via-uri uri)
            component (assoc this :mongo mongo)]
        (mg/set-default-write-concern! WriteConcern/MAJORITY)
        component)))
  (stop [this]
    (if (:mongo this)
      (stop this)
      this)))

(defn connect-db [config]
  (map->Db config))
