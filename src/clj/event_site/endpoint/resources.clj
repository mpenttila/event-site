(ns event-site.endpoint.resources
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [content-type resource-response redirect]]))

(defn unauthorized-response []
  {:status 401
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body "Sisäänkirjautuminen vaaditaan"})

(defn forbidden-response []
  {:status 403
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body "Sinulla ei ole tarvittavia käyttöoikeuksia"})

(defn authorized? [request]
  (get-in request [:session :authorized]))

(defn wrap-authorization [handler]
  (fn [request]
    (if (authorized? request)
      (let [response (handler request)]
        (when response
          (assoc response :session (or (:session response) (:session request)))))
      (unauthorized-response))))

(defn resources [{{:keys [common-password]} :security}]
  (routes
    (GET "/" request
      (let [index-file (if (authorized? request)
                         "private/secure-index.html"
                         "public/index.html")]
        (-> (resource-response index-file)
            (content-type "text/html"))))
    (GET "/admin" request
      (if (authorized? request)
        (-> (resource-response "private/secure-index.html")
            (content-type "text/html"))
        (redirect "/")))
    (POST "/login" {{password :password} :params}
      (cond-> (redirect "/" :see-other)
              (= common-password password)  (assoc-in [:session :authorized] true)))
    (route/resources "/" {:root "public"})
    (-> (route/resources "/" {:root "private"})
        (wrap-authorization))))
