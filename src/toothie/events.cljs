(ns toothie.events
  (:require
   [re-frame.core :as re]
   [clojure.spec.alpha :as s]
   [toothie.db :as db :refer [app-db]]))

(def ReactNative (js/require "react-native"))
(def Vibration (.-Vibration ReactNative))

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
  (fn [start-time]
    (let [start-ms   (.getMilliseconds start-time)
          current-ms (.getMilliseconds (js/Date.))
          ;; If we have drifted to the next second
          current-ms (if (< current-ms start-ms) (+ 1000 current-ms) current-ms)
          sleep-time (+ 1000 (- start-ms current-ms))]
      ;; start 983 => current 902
      (js/setTimeout #(re/dispatch [::tick]) sleep-time))))

(re/reg-fx ::vibrate
  (fn []
    (println "vibrate")
    (.vibrate Vibration)))

;; -- Handlers --------------------------------------------------------------

(re/reg-event-fx ::initialize-db
  validate-spec
  (fn [_ _]
    (let [{:keys [start-time] :as db} (app-db)]
      {:db db
       ::timer-tick start-time})))

(re/reg-event-fx ::tick
  validate-spec
  (fn [{:keys [db]}]
    (let [{:keys [start-time]} db
          current-time (js/Date.)
          diff (js/Math.round (/ (- (.getTime current-time) (.getTime start-time)) 1000))
          should-continue? (and (not (get db :stop false)) (< diff 120))
          should-vibrate? (and (zero? (mod diff 30)) (> diff 0))]
      (merge
        {:db (assoc db :current-time current-time)}
        (when should-vibrate?  {::vibrate nil})
        (when should-continue? {::timer-tick start-time})))))

(re/reg-event-db ::stop
  (fn [db]
    (assoc db :stop true)))
