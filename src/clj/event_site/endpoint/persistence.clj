(ns event-site.endpoint.persistence
  (:require [monger.collection :as mc]
            [event-site.domain :as domain]
            [compojure.core :refer :all]
            [ring.middleware.transit :refer [wrap-transit-body wrap-transit-response]]
            [monger.query :as q]
            [postal.core :as postal])
  (:import (java.util Date)))

(def collection "registrations")

(defn send-confirmation-email [email]
  (postal/send-message {:from "info@domain.local"
                        :to email
                        :subject "Thank you and welcome!"
                        :body "Test."}))

(defn store-registration [db data]
  (let [data-with-ts (assoc data :created (Date.))]
    (domain/validate data-with-ts)
    (mc/insert db collection data-with-ts)
    (send-confirmation-email (:email data))))

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
    (-> (routes (GET "/registrations" request
                  (if-let [verified-password (verify-password request admin-password)]
                    (let [regs (->> (get-registrations db)
                            (map #(dissoc % :_id)))]
                      {:status 200
                       :body regs
                       :session (assoc (:session request) :admin-password verified-password)})
                    {:status 403
                     :headers {"Content-Type" "text/plain; charset=utf-8"}
                     :body "Sinulla ei ole tarvittavia käyttöoikeuksia"}))
                (GET "/registration-data" {{:keys [registration]} :session}
                  (if registration
                    {:status 200
                     :body registration}
                    {:status 404
                     :body "No registration data"})))
        (wrap-transit-response))
    (-> (POST "/register" {:keys [body session]}
          (try
            (let [reg-data (select-keys body [:name :email :food :other])]
              (store-registration db reg-data)
              {:status 200
               :body "Registration saved"
               :session (merge session {:registration reg-data})})
            (catch Throwable e
              {:status 400
               :body "Bad request"})))
        (wrap-transit-body))))
