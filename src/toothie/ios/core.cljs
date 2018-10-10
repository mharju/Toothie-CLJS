(ns toothie.ios.core
  (:require [reagent.core :as r]
            [re-frame.core :as re]
            [toothie.events :as events]
            [toothie.subs :as sub]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def app-state (.-AppState ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))

(defn app-root []
  (r/with-let [stopwatch (re/subscribe [::sub/stopwatch])
        section (re/subscribe [::sub/section-text])
        section-color (re/subscribe [::sub/section-color])]
    [view {:style {:flex 1 :flex-direction "column" :align-items "center" :justify-content :center :background-color @section-color}}
       [text {:style {:font-size 80 :font-weight "100" :margin-bottom 20 :text-align "center"}}
         (str @stopwatch)]
       [text {:style {:font-size 30 :font-weight "600" :margin-bottom 20 :text-align "center"}}
         (str @section)]]))

(defn init []
  (re/dispatch-sync [::events/initialize-db])
  (.addEventListener app-state "change" (fn [] (re/dispatch [::events/initialize-db])))
  (.registerComponent app-registry "toothie" #(r/reactify-component app-root)))
