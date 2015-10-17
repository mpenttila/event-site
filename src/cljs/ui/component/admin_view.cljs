(ns ui.component.admin-view
  (:require [reagent.core :as reagent]
            [ui.state :as state]
            [clojure.string :as string]
            [ui.utils :refer [handle-change]]))

(defn admin-view []
  (let [admin-password (reagent/atom "")]
    (fn []
      (let [{:keys [authorized? list]} @state/registrations]
        [:div.app-container
         [:div.content
          [:div.content-section
           [:div.markup-container
            [:h1 "Registrations"]
            (if authorized?
              [:table.registrations
               [:tr
                [:th "Name"]
                [:th "Email"]
                [:th "Allergies"]
                [:th "Other info"]]
               (doall
                 (for [{:keys [name email food other]} list]
                   ^{:key email} [:tr
                                  [:td name]
                                  [:td email]
                                  [:td food]
                                  [:td other]]))]
              [:form {:on-submit (fn [e]
                                   (.preventDefault e)
                                   (state/get-registrations @admin-password))}
               [:label "Please enter admin password"
                [:input {:type "password"
                         :value @admin-password
                         :on-change (handle-change admin-password)}]
                [:button {:type "submit" :disabled (string/blank? @admin-password)} "Submit"]]])]]]]))))
