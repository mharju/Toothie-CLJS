(ns toothie.events
  (:require
   [re-frame.core :as re]
   [clojure.spec.alpha :as s]
   [toothie.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (re/after (partial check-and-throw ::db/app-db))
    []))

;; -- Effects --------------------------------------------------------------

(re/reg-fx ::timer-tick
  (fn []
    (js/setTimeout #(re/dispatch [::tick]) 1000)))

;; -- Handlers --------------------------------------------------------------

(re/reg-event-fx ::initialize-db
  validate-spec
  (fn [_ _]
    {:db (app-db)
     ::timer-tick nil}))

(re/reg-event-fx ::tick
  validate-spec
  (fn [{:keys [db]}]
    (let [{:keys [current-time start-time]} db
          diff (/ (- (.getTime current-time) (.getTime start-time)) 1000)
          should-continue? (< diff 119)]
      (merge
        {:db (assoc db :current-time (js/Date.))}
        (when should-continue?
           { ::timer-tick nil})))))
