(ns go-reagent.logic)

(defn new-board [size]
  (zipmap
    (for [i (range size)
          j (range size)]
      [i j])
    (repeat :empty)))

(defn new-game-state [size]
  {:current-color :black
   :size size 
   :board (new-board size)
   :last-move-passed false
   :in-atari false
   :attempted-suicide false
   :game-over false
   })

(defn switch-player [game-state]
  (assoc game-state 
         :current-color
         (if (= (:current-color game-state) :black)
           :white
           :black)))

(defn pass [game-state]
  (-> game-state
      (#(if (:last-move-passed %) 
          (assoc % :game-over true)
          %))
      (assoc :last-move-passed true)
      switch-player))


(defn play-move [game-state move]
  (if-not (= :empty (get-in game-state [:board move]))
    [false game-state]
    (let [color (:current-color game-state)
          new-state (-> game-state
                        (assoc-in [:board move] color)
                        switch-player)]
      [true new-state])))
