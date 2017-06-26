(ns go-reagent.core
  (:require
    [reagent.core :as r]))

(js/alert "Hello Clojurescript!")

(defn Game []
  [:div "Hi"])

(r/render
  [Game]
  (js/document.getElementById "container"))
