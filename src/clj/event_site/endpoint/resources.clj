(ns event-site.endpoint.resources
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [content-type resource-response]]
            ))

(defn resources [_]
  (routes
    (GET "/" [] (-> (resource-response "public/index.html")
                    (content-type "text/html")))
    (route/resources "/")))

