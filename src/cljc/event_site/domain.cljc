(ns event-site.domain
  (:require [schema.core :as s]
            [clojure.string :as string]))

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

(defn validate [registration-data]
  (s/validate Registration registration-data))
