(ns ui.state
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET]]))

(def content-ratom (reagent/atom []))

(def registrations (reagent/atom {:list []
                                  :authorized? false}))

(def registration-data (reagent/atom {}))

(def registration-state (reagent/atom :will-open))

(defn load-content [content-vec]
  (reset! content-ratom content-vec))

(defn get-registrations [password]
  (GET "/registrations"
       {:params (if password {:password password} {})
        :handler #(swap! registrations assoc :list % :authorized? true)
        :error-handler (fn [{:keys [status]}]
                         (when (= 401 status)
                           #(swap! registrations assoc :authorized? false)))}))

(defn load-previous-data []
  (GET "/registration-data"
       {:handler (fn [response]
                   (reset! registration-data (:data response))
                   (reset! registration-state (:open response)))}))
