(ns toothie.db
  (:require [clojure.spec.alpha :as s]))

(s/def ::current-time inst?)
(s/def ::app-db
  (s/keys :req-un [::current-time ::start-time]))

(defn app-db []
  (let [date (js/Date.)]
    {:start-time date :current-time date}))
