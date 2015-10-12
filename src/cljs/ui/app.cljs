(ns ui.app
  (:require [ui.utils :as utils]
            [ui.content.karonkka.content :refer [pages]]
            [reagent.core :as reagent]
            [goog.events :as events]
            [goog.events.EventType :as EventType]
            [clojure.walk :as walk]
            [ui.component.register-form :as register-form]))

(def cur-scroll-y (reagent/atom 0))

(def offsets (reagent/atom {}))

(def content-ratom (reagent/atom []))

(defn load-content [content-vec]
  (reset! content-ratom content-vec))

(defn render-components [markup]
  (walk/postwalk
    (fn [form]
      (cond
        (= :register-form form) register-form/form-component
        :else form)) markup))

(def content-section
  (with-meta
    (fn [{:keys [id markup style]}]
      [:div.content-section {:id id :style style}
       [:div.markup-container
        (render-components markup)]])
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

(defn link-text [id]
  (apply str (.toUpperCase (first id)) (rest id)))

(def main-view
  (with-meta
    (let [drawer-open? (reagent/atom false)]
      (fn []
        (let [scroll-y @cur-scroll-y
              content @content-ratom]
          (when (seq content)
            (let [menu-links (doall
                               (for [{:keys [id]} content]
                                 (let [{:keys [top bottom]} (get @offsets id)
                                       active? (and (>= scroll-y (- top 40))
                                                    (< scroll-y (- bottom 41)))]
                                   ^{:key (str id "-link")}
                                   [:li
                                    [:a {:href (str "#" id)
                                         :class (when active? "active")
                                         :on-click (fn [e]
                                                     (reset! drawer-open? false)
                                                     (handle-link-click id e))}
                                     (link-text id)]])))]
              [:div.app-container
               [:div.menu-wrapper
                [:nav.menu
                 [:div.mobile
                  [:i.icon-menu {:on-click (fn [_] (swap! drawer-open? not)
                                             true)}]
                  (when @drawer-open?
                    [:div.menu-drawer
                     [:ul menu-links]])]
                 [:ul.desktop menu-links]]]
               [:div.content
                (doall
                  (for [{:keys [id] :as page} content]
                    ^{:key id} [content-section page]))]])))))
    {:component-did-mount (fn [_]
                            (events/listen js/window EventType/SCROLL
                                           (fn [e] (reset! cur-scroll-y (.-scrollY js/window)))))}))

(defn ^:export start []
  (enable-console-print!)
  (load-content pages)
  (reagent/render-component [main-view] (.getElementById js/document "app")))
