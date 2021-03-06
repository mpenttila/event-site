(ns ui.component.register-form
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [event-site.domain :as domain]
            [ajax.core :refer [POST GET]]
            [ui.utils :refer [handle-change]]
            [ui.state :refer [registration-data registration-state]]
            [ui.utils :as utils]))

(defn handle-submit [data]
  (POST "/register"
        {:params data
         :handler #(reset! registration-data data)}))

(defn form-component []
  (let [name (reagent/atom (or (:name @registration-data) ""))
        email (reagent/atom (or (:email @registration-data) ""))
        food (reagent/atom (or (:food @registration-data) ""))
        other (reagent/atom (or (:other @registration-data) ""))
        valid? (reaction (try
                           (domain/validate {:name @name
                                             :email @email
                                             :food @food
                                             :other @other})
                           (catch js/Object e
                             false)))]
    (fn []
      (let [already-registered? (not (empty? @registration-data))
            reg-state @registration-state
            nv (or (:name @registration-data) @name)
            ev (or (:email @registration-data) @email)
            fv (or (:food @registration-data) @food)
            ov (or (:other @registration-data) @other)]
        (case reg-state
          :open [:form.register-form {:on-submit (fn [e]
                                                   (.preventDefault e)
                                                   (handle-submit {:name @name
                                                                   :email @email
                                                                   :food @food
                                                                   :other @other})
                                                   (reset! name "")
                                                   (reset! email "")
                                                   (reset! food "")
                                                   (reset! other ""))}
                 [:div.input-fields
                  (when utils/is-old-ie? [:div.ie-label "Name"])
                  [:input {:placeholder "Name" :value nv :disabled already-registered? :on-change (handle-change name)}]
                  (when utils/is-old-ie? [:div.ie-label "Email"])
                  [:input {:placeholder "Email" :value ev :disabled already-registered? :on-change (handle-change email)}]
                  (when utils/is-old-ie? [:div.ie-label "Allergies"])
                  [:input {:placeholder "Allergies" :value fv :disabled already-registered? :on-change (handle-change food)}]
                  (when utils/is-old-ie? [:div.ie-label "Other info"])
                  [:textarea {:placeholder "Other info" :value ov :disabled already-registered? :on-change (handle-change other)}]]
                 (if-not already-registered?
                   [:div
                    [:button {:type "submit" :disabled (not @valid?)} "Send"]]
                   [:div.already-registered
                    [:div "Thank you for registering!"]
                    [:div
                     [:a {:href "#"
                          :on-click (fn [e]
                                      (.preventDefault e)
                                      (reset! registration-data {}))}
                      "Register another person"]]])]
          :will-open [:div "Registration has not yet started"]
          [:div "Registration has ended"])))))