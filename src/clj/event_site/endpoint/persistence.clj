(ns event-site.endpoint.persistence
  (:require [monger.collection :as mc]
            [event-site.domain :as domain]
            [compojure.core :refer :all]
            [ring.middleware.transit :refer [wrap-transit-body wrap-transit-response]]
            [monger.query :as q]
            [postal.core :as postal]
            [clojure.string :as string])
  (:import (java.util Date)
           (java.time ZonedDateTime)
           (java.time.format DateTimeFormatter)))

(def collection "registrations")

(defn send-confirmation-email [reg-data email-config]
  (let [mail (-> email-config
                 (assoc :to (:email reg-data))
                 (assoc :body (string/replace (:body email-config) "[NAME]" (:name reg-data))))]
    (postal/send-message mail)))

(defn store-registration [db data email-config]
  (let [data-with-ts (assoc data :created (Date.))]
    (domain/validate data-with-ts)
    (mc/insert db collection data-with-ts)
    (send-confirmation-email data email-config)))

(defn get-registrations [db]
  (q/with-collection db collection
    (q/find {})
    (q/sort (array-map :created 1))))

(defn verify-password [request admin-password]
  (let [password (or (get-in request [:session :admin-password]) (get-in request [:params :password]))]
    (when (= password admin-password)
      password)))

(defn persistence-routes [{{{db :db} :mongo} :db {:keys [admin-password]} :security email-config :email registration-config :registration}]
  (routes
    (-> (routes (GET "/registrations" request
                  (if-let [verified-password (verify-password request admin-password)]
                    (let [regs (->> (get-registrations db)
                            (map #(dissoc % :_id)))]
                      {:status 200
                       :body regs
                       :session (assoc (:session request) :admin-password verified-password)})
                    {:status 401
                     :headers {"Content-Type" "text/plain; charset=utf-8"}
                     :body "Invalid or missing authorization"}))
                (GET "/registration-data" {{:keys [registration]} :session}
                  (let [start-dt (ZonedDateTime/parse (:start registration-config) DateTimeFormatter/ISO_OFFSET_DATE_TIME)
                        end-dt (ZonedDateTime/parse (:end registration-config) DateTimeFormatter/ISO_OFFSET_DATE_TIME)
                        now (ZonedDateTime/now)
                        reg-state (cond
                                    (.isBefore now start-dt) :will-open
                                    (.isAfter now end-dt) :has-closed
                                    :else :open)]
                    {:status 200
                     :body {:data (or registration {})
                            :open reg-state}})))
        (wrap-transit-response))
    (-> (POST "/register" {:keys [body session]}
          (try
            (let [reg-data (select-keys body [:name :email :food :other])]
              (store-registration db reg-data email-config)
              {:status 200
               :body "Registration saved"
               :session (merge session {:registration reg-data})})
            (catch Throwable e
              {:status 400
               :body "Bad request"})))
        (wrap-transit-body))))
