(ns go-reagent.core
  (:require
    [reagent.core :as r]
    [go-reagent.views :as v]
    [go-reagent.logic :as l]))

(def game-state (r/atom (l/new-game-state 9)))

(defn click [coord]
  (swap! game-state #(second (l/play-move % coord))))

(defn app []
  [v/Game @game-state click])


(r/render
  [app]
  (js/document.getElementById "container"))
