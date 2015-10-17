(ns event-site.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [event-site.component.db :refer [connect-db]]
            [event-site.endpoint.resources :refer [resources]]
            [event-site.endpoint.persistence :refer [persistence-routes]]
            [ring.middleware.session.cookie :as rmsc]))

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
  (let [cookie-secret (get-in config [:security :cookie-secret])
        config (-> base-config
                   (meta-merge {:app {:defaults {:session {:store (rmsc/cookie-store {:key cookie-secret})}}}})
                   (meta-merge config))]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :db (connect-db (:db config))
         :resources (endpoint-component resources)
         :persistence (endpoint-component persistence-routes)
         :security (:security config))
        (component/system-using
         {:http [:app]
          :app  [:resources :persistence]
          :persistence [:db]
          :resources [:security]}))))
