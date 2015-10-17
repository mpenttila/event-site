(ns event-site.endpoint.persistence
  (:require [monger.collection :as mc]
            [event-site.domain :as domain]
            [compojure.core :refer :all]
            [ring.middleware.transit :refer [wrap-transit-body wrap-transit-response]]
            [monger.query :as q])
  (:import (java.util Date)))

(def collection "registrations")

(defn store-registration [db data]
  (let [data-with-ts (assoc data :created (Date.))]
    (domain/validate data-with-ts)
    (mc/insert db collection data-with-ts)))

(defn get-registrations [db]
  (q/with-collection db collection
    (q/find {})
    (q/sort (array-map :created 1))))

(defn verify-password [request admin-password]
  (let [password (or (get-in request [:session :admin-password]) (get-in request [:params :password]))]
    (when (= password admin-password)
      password)))

(defn persistence-routes [{{{db :db} :mongo} :db {:keys [admin-password]} :security}]
  (routes
    (-> (GET "/registrations" request
          (if-let [verified-password (verify-password request admin-password)]
            (let [regs (->> (get-registrations db)
                            (map #(dissoc % :_id)))]
              {:status 200
               :body regs
               :session (assoc (:session request) :admin-password verified-password)})
            {:status 403
             :headers {"Content-Type" "text/plain; charset=utf-8"}
             :body "Sinulla ei ole tarvittavia käyttöoikeuksia"}))
        (wrap-transit-response))
    (-> (POST "/register" {:keys [body]}
          (try
            (store-registration db (select-keys body [:name :email :food :other]))
            {:status 200
             :body "Registration saved"}
            (catch Throwable e
              {:status 400
               :body "Bad request"})))
        (wrap-transit-body))))
