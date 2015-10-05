(ns event-site.endpoint.persistence
  (:require [monger.collection :as mc]
            [schema.core :as s]
            [clojure.string :as string]
            [compojure.core :refer :all]))

(def NonEmptyStr (s/both s/Str (s/pred #(not (string/blank? %)) 'non-empty-string)))

(defn- matches [r]
  (s/pred (fn [s] (re-matches r s))))

(defn- min-length [l]
  (s/pred (fn [x] (>= (count x) l))))

(def Email (s/both (min-length 5) (matches #".*@.*")))

(def Registration {:name NonEmptyStr
                   :email Email
                   :food s/Str
                   :other s/Str})

(defn store-registration [db data]
  (s/validate Registration data)
  (mc/insert db "registrations" data))

(defn persistence-routes [db]
  (routes
    (POST "/register" {params :params}
      (try
        (store-registration db (select-keys params [:name :email :food :other]))
        (catch Throwable e
          {:status 400
           :body "Bad request"}))
      {:status 200
       :body "Registration saved"})))
