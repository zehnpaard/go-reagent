(ns go-reagent.views
  (:require
    [reagent.core :as r]))

(def GRID_SIZE 40)

(defn BoardIntersection [[row col] color click-fn]
  (let [style {:top (* row GRID_SIZE)
               :left (* col GRID_SIZE)}
        classes (str "intersection" 
                     (cond (= color :black) " black"
                           (= color :white) " white"
                           :else            ""))]
    [:div {:className classes 
           :style style 
           :onClick #(click-fn [row col])}]))

(defn Game []
  [:div
   [BoardIntersection [2 3] :white (fn [[row col]] (js/alert (str row "," col)))]])
