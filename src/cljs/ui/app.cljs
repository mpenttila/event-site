(ns ui.app
  (:require [ui.utils :as utils]
            [reagent.core :as reagent]
            [goog.events :as events]
            [goog.events.EventType :as EventType]))

(def cur-scroll-y (reagent/atom 0))

(def offsets (reagent/atom {}))

(def pages ["home" "portfolio" "about" "contact"])

(def content-section
  (with-meta
    (fn [id]
      [:div {:id id}])
    {:component-did-mount (fn [this]
                            (let [node (reagent/dom-node this)
                                  top (.-offsetTop node)
                                  bottom (+ (.-offsetHeight node) top)
                                  id (.-id node)]
                              (swap! offsets assoc id {:top top :bottom bottom})))}))

(defn handle-link-click [id e]
  (.preventDefault e)
  (let [{:keys [top]} (get @offsets id)
        scroll-y (.-scrollY js/window)
        diff (- top scroll-y)
        duration 1201.0
        epsilon (/ 1000 60 duration 4)
        ease-fn (utils/bezier 0.25 1 0.25 1 epsilon)]

    (.requestAnimationFrame js/window (fn [start]
                                        (letfn [(animate-frame [ts] (let [dt (- ts start)
                                                                          progress (* 1.05 (/ dt duration))
                                                                          bezier-term (ease-fn progress)
                                                                          position (+ scroll-y (Math/round (* bezier-term diff)))]
                                                                      (.scrollTo js/window 0 position)
                                                                      (when-not (= position top)
                                                                        (.requestAnimationFrame js/window animate-frame))))]
                                          (animate-frame start))))))

(def main-view
  (with-meta
    (fn []
      (let [scroll-y @cur-scroll-y]
        [:div.app-container
         [:div.menu-wrapper
          [:nav.menu
           [:ul
            (doall
              (for [id pages]
                (let [{:keys [top bottom]} (get @offsets id)]
                    ^{:key (str id "-link")} [:li
                                              [:a {:href (str "#" id)
                                                   :class (when (and (>= scroll-y top) (< scroll-y bottom)) "active")
                                                   :on-click (partial handle-link-click id)}
                                               id]])))]]]
         [:div.content
          (doall
            (for [id pages]
              ^{:key id} [content-section id]))]]))
    {:component-did-mount (fn [this]
                            (events/listen js/window EventType/SCROLL
                                           (fn [e] (reset! cur-scroll-y (.-scrollY js/window)))))}))

(defn ^:export start []
  (enable-console-print!)
  (reagent/render-component [main-view] (.getElementById js/document "app")))
