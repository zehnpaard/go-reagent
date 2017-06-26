(ns go-reagent.core
  (:require
    [reagent.core :as r]
    [go-reagent.views :as v]
    [go-reagent.logic :as l]))

(js/alert "Hello Clojurescript!")

(r/render
  [v/Game]
  (js/document.getElementById "container"))
