(ns ui.state
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET]]))

(def content-ratom (reagent/atom []))

(def registrations (reagent/atom {:list []
                                  :authorized? false}))

(defn load-content [content-vec]
  (reset! content-ratom content-vec))

(defn get-registrations [password]
  (GET "/registrations"
       {:params (if password {:password password} {})
        :handler #(swap! registrations assoc :list % :authorized? true)
        :error-handler (fn [{:keys [status]}]
                         (when (= 401 status)
                           #(swap! registrations assoc :authorized? false)))}))
