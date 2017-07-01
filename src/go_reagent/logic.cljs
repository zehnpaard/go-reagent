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

(defn get-group [move game-state]
  (let [color (get-in game-state [:board move])]
    (if-not (= color :empty)
      (loop [visited #{}
             visited-list []
             queue [move]
             liberties 0]
        (cond 
          (empty? queue)
          {:liberties liberties
           :stones visited-list}

          (visited (first queue))
          (recur visited visited-list (rest queue) liberties)

          :else
          (let [current   (first queue)
                neighbors (get-adjacent game-state current)
                empties   (filter #(= :empty (get-in game-state [:board %]))
                                  neighbors)
                sames     (filter #(= color (get-in game-state [:board %]))
                                  neighbors)]
            (recur 
              (conj visited current)
              (conj visited-list current)
              (into (rest queue) sames)
              (+ liberties (count empties)))))))))

(defn remove-stone [game-state coord]
  (assoc-in game-state [:board coord] :empty))

(defn remove-stones [game-state coords]
  (reduce remove-stone game-state coords))

(defn play-move [game-state move]
  (if-not (= :empty (get-in game-state [:board move]))
    game-state
    (let [color (:current-color game-state)
          new-state (-> game-state
                        (assoc-in [:board move] color)
                        switch-player)
          neighbors (get-adjacent game-state move)
          n-others  (filter #(and (not= :empty (get-in new-state [:board %]))
                                  (not= color (get-in new-state [:board %]))) 
                            neighbors)
          n-groups  (map #(get-group % new-state) n-others)
          captured  (filter #(zero? (:liberties %)) n-groups)
          atari     (some #(= 1 (:liberties %)) n-groups)]
      (if (and (empty? captured)
               (-> move (get-group new-state) :liberties zero?))
       (-> game-state
         (assoc :in-atari false)
         (assoc :attempted-suicide true))
       (-> new-state
           (remove-stones (->> captured (map :stones) (apply concat)))
           (assoc :in-atari atari)
           (assoc :last-move-passed false))))))
