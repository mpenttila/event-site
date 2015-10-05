(ns ui.component.register-form
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [event-site.domain :as domain]
            [ajax.core :refer [POST]]))

(def already-registered? (reagent/atom false))

(defn handle-change [atom]
  (fn [e]
    (let [value (.. e -target -value)]
      (reset! atom value))))

(defn handle-submit [data]
  (println data)
  (POST "/register"
        {:params data
         :handler #(reset! already-registered? true)}))

(defn form-component []
  (let [name (reagent/atom "")
        email (reagent/atom "")
        food (reagent/atom "")
        other (reagent/atom "")
        valid? (reaction (try
                           (domain/validate {:name @name
                                             :email @email
                                             :food @food
                                             :other @other})
                           (catch js/Error e
                             false)))]
    (fn []
      [:form.register-form {:on-submit (fn [e]
                                         (.preventDefault e)
                                         (handle-submit {:name @name
                                                         :email @email
                                                         :food @food
                                                         :other @other}))}
       [:div.input-fields
        [:input {:placeholder "Name" :value @name :on-change (handle-change name)}]
        [:input {:placeholder "Email" :value @email :on-change (handle-change email)}]
        [:input {:placeholder "Allergies" :value @food :on-change (handle-change food)}]
        [:textarea {:placeholder "Other info" :value @other :on-change (handle-change other)}]]
       (if-not @already-registered?
         [:div
          [:button {:type "submit" :disabled (not @valid?)} "Send"]]
         [:div.already-registered
          "Thank you for registering!"])])))