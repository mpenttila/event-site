(ns ui.app
  (:require [ui.content.karonkka.content :refer [pages]]
            [ui.component.main-view :refer [main-view]]
            [ui.component.admin-view :refer [admin-view]]
            [reagent.core :as reagent]
            [ui.state :as state]))

(defn app-root []
  (let [current-url (-> js/window .-location .-href)
        admin-view? (> (.indexOf current-url "/admin") 0)]
    (if admin-view?
      (do (state/get-registrations nil)
          [admin-view])
      [main-view])))

(defn ^:export start []
  (enable-console-print!)
  (state/load-content pages)
  (state/load-previous-data)
  (reagent/render-component [app-root] (.getElementById js/document "app")))
