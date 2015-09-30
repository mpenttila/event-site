(ns ui.app
  (:require [reagent.core :as reagent]))

(defn main-view []
  [:div.app-container
   [:div.wrapper {:id "wrapper"}
    [:h1 "Moro moro"]]])

(defn ^:export start []
  (reagent/render-component [main-view] (.getElementById js/document "app")))
