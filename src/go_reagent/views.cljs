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

(defn BoardView [game-state click-fn]
  (let [size (:size game-state)
        style {:width (* size GRID_SIZE)
               :height (* size GRID_SIZE)}
        main-div [:div {:id "board"
                        :style style}]
        coords (for [x (range size) y (range size)] [x y])
        make-intersection (fn [coord]
                            [BoardIntersection coord (get-in game-state [:board coord]) click-fn])
        intersections (for [coord coords]
                        ^{:key coord} (make-intersection coord))]
    (into main-div intersections)))

(defn PassView [pass-fn]
  [:input {:id "pass-btn"
           :type "button"
           :value "Pass"
           :onClick pass-fn}])

(defn Game [game-state click-fn pass-fn]
  [:div
   [PassView pass-fn]
   [BoardView game-state click-fn]])
