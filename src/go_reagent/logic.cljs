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
  (update game-state 
         :current-color
         #(if (= :black %) :white :black)))

(defn pass [game-state]
  (-> game-state
      (#(if (:last-move-passed %) 
          (assoc % :game-over true)
          %))
      (assoc :last-move-passed true)
      switch-player))

(defn valid-coord [game-state coord]
  (and
    (every? #(> % -1) coord)
    (every? #(< % (:size game-state)) coord)))

(defn get-adjacent [game-state [i j]]
  (->> [[i (dec j)]
        [i (inc j)]
        [(dec i) j]
        [(inc i) j]]
       (filter #(valid-coord game-state %))))

(defn get-group [game-state move]
  (let [color (get-in game-state [:board move])]
    (if-not (= color :empty)
      (loop [visited #{}
             queue [move]]
        (cond 
          (empty? queue)
          visited

          (visited (first queue))
          (recur visited (rest queue))

          :else
          (let [current   (first queue)
                neighbors (get-adjacent game-state current)
                sames     (filter #(= color (get-in game-state [:board %]))
                                  neighbors)]
            (recur 
              (conj visited current)
              (into (rest queue) sames))))))))

(defn count-liberties [game-state stones]
  true)

(defn remove-stone [game-state coord]
  (assoc-in game-state [:board coord] :empty))

(defn remove-stones [game-state coords]
  (reduce remove-stone game-state coords))

(defn play-move [game-state move]
  (if-not (= :empty (get-in game-state [:board move]))
    [false game-state]
    (let [color (:current-color game-state)
          new-state (-> game-state
                        (assoc-in [:board move] color)
                        switch-player)
          neighbors (get-adjacent game-state move)
          n-other   (filter #(and (not= :empty (get-in new-state [:board %]))
                                  (not= color (get-in new-state [:board %]))) 
                            neighbors)
          n-groups  (map #(get-group new-state %) n-other)
          captured  (filter #(zero? (count-liberties new-state %)) n-groups)
          atari     (some #(= 1 (count-liberties new-state %)) n-groups)]
      (if (and (empty? captured)
               (->> move (get-group new-state) (count-liberties new-state) zero?))
        [false
         (-> game-state
           (assoc :in-atari false)
           (assoc :attempted-suicide true))]
        [true
         (-> new-state
             (remove-stones (apply concat captured))
             (assoc :in-atari atari)
             (assoc :last-move-passed false))]))))
