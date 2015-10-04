(ns event-site.main
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [duct.middleware.errors :refer [wrap-hide-errors]]
            [meta-merge.core :refer [meta-merge]]
            [event-site.system :refer [new-system]]
            [clojure.edn :as edn]))

(def prod-config
  {:app {:middleware     [[wrap-hide-errors :internal-error]]
         :internal-error "Internal Server Error"}})

(defn -main [& args]
  (when-let [config (-> (edn/read-string (slurp (or (first args) "config.edn")))
                        (meta-merge prod-config))]
    (let [system (new-system config)]
      (println "Starting HTTP server on port" (-> system :http :port))
      (component/start system))))
