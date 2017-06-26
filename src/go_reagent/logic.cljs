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

(defn get-adjacent [game-state [i j]]
  (->> [[i (dec j)]
        [i (inc j)]
        [(dec i) j]
        [(inc i) j]]
       (filter #(> (first %) -1))
       (filter #(< (first %) (:size game-state)))
       (filter #(> (second %) -1))
       (filter #(< (second %) (:size game-state)))))

(defn get-group [stone game]
  (let [color (get-in game [:board stone])
        data  {:visited #{}
               :visited_list []
               :queue [stone]
               :count 0}]
    (if-not (= color :empty)
      (loop [d data]
        (cond 
          (empty? (:queue d))
          {:liberties (:count d)
           :stones (:visited_list d)}

          ((:visited d) (first (:queue d)))
          (recur (update d :queue rest))

          :else
          (let [current   (first (:queue d))
                neighbors (get-adjacent current game)
                empties   (filter #(= :empty (get-in game [:board %]))
                                  neighbors)
                sames     (filter #(= color (get-in game [:board %]))
                                  neighbors)
                new-data  (-> d
                              (update :queue rest)
                              (update :count #(+ % (count empties)))
                              (update :queue #(into % sames)))]
            (recur new-data)))))))

(defn remove-stone [game stone]
  (assoc-in game [:board stone] :empty))

(defn remove-stones [game stones]
  (reduce remove-stone game stones))



(defn play-move [game-state move]
  (if-not (= :empty (get-in game-state [:board move]))
    [false game-state]
    (let [color (:current-color game-state)
          new-state (-> game-state
                        (assoc-in [:board move] color)
                        switch-player)]
      [true new-state])))
