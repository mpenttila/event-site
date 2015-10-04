(ns event-site.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.component.hikaricp :refer [hikaricp]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [event-site.endpoint.resources :refer [resources]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-defaults :defaults]]
         :not-found  "Resource Not Found"
         :defaults   {:params    {:urlencoded true
                                  :multipart  true
                                  :nested     true
                                  :keywordize true}
                      :cookies   true
                      :session   {:flash true
                                  :cookie-attrs {:http-only true}}
                      :security  {:anti-forgery   false
                                  :xss-protection {:enable? true, :mode :block}
                                  :frame-options  :sameorigin
                                  :content-type-options :nosniff}
                      :responses {:not-modified-responses false
                                  :absolute-redirects     true
                                  :content-types          true
                                  :default-charset        "utf-8"}}}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         ;:db   (hikaricp (:db config))
         :resources (endpoint-component resources))
        (component/system-using
         {:http [:app]
          :app  [:resources]
          :resources []}))))
