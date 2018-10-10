(ns toothie.subs
  (:require [re-frame.core :as re]))

(re/reg-sub
  ::start-time
  (fn [{:keys [start-time]} _]
    start-time))

(re/reg-sub
  ::current-time
  (fn [{:keys [current-time]} _]
    current-time))

(re/reg-sub ::diff
  :<- [::start-time]
  :<- [::current-time]
  (fn [[start-time current-time]]
    (/ (- (.getTime current-time) (.getTime start-time)) 1000)))

(re/reg-sub ::stopwatch
  :<- [::diff]
  (fn [diff]
    (let [minutes (js/Math.floor (/ diff 60))
          seconds (js/Math.floor (- diff (* minutes 60)))]
      (str (when (< minutes 10) "0") minutes ":" (when (< seconds 10) "0") seconds))))

(re/reg-sub ::section
  :<- [::diff]
  (fn [diff]
    (js/Math.floor (/ diff 30))))

(re/reg-sub ::section-text
  :<- [::section]
  (fn [section]
    (case section
      0 "low left"
      1 "low right"
      2 "top right"
      3 "top left"
      "done!")))

(re/reg-sub ::section-color
  :<- [::section]
  (fn [section]
    (case section
      0 "#ebb3a9"
      1 "#e87ea1"
      2 "#e86252"
      "#ee2677")))
