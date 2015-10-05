(ns event-site.endpoint.persistence
  (:require [monger.collection :as mc]
            [event-site.domain :as domain]
            [compojure.core :refer :all]
            [ring.middleware.transit :refer [wrap-transit-body]]))

(defn store-registration [db data]
  (domain/validate data)
  (mc/insert db "registrations" data))

(defn persistence-routes [{{{db :db} :mongo} :db}]
  (routes
    (-> (POST "/register" {:keys [body]}
          (try
            (store-registration db (select-keys body [:name :email :food :other]))
            {:status 200
             :body "Registration saved"}
            (catch Throwable e
              {:status 400
               :body "Bad request"})))
        (wrap-transit-body))))
