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

(defn reverse-color [color]
   (if (= :black color) :white :black))

(defn switch-player [game-state]
  (update game-state 
         :current-color
         reverse-color))

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

(defn get-color [game-state coord]
  (get-in game-state [:board coord]))

(defn is-color [game-state color coord]
  (= color (get-color game-state coord)))

(defn iterate-get-first [iter-fn pred initial]
  (->> initial
       (iterate iter-fn)
       (filter pred)
       first))

(defn get-group [game-state move]
  (let [color (get-in game-state [:board move])
        iter-fn (fn [[[cur & tail] visited]]
                  (if (visited cur)
                    [tail visited]
                    [(->> cur
                          (get-adjacent game-state)
                          (filter #(is-color game-state color %))
                          (into tail))
                     (conj visited cur)]))
        init-state  [[move] #{}]
        all-checked (fn [state] (empty? (first state)))]
    (->> init-state
         (iterate-get-first 
           iter-fn 
           all-checked)
         second
         seq
         )))

(defn count-liberties [game-state stones]
  (->> stones
       (map #(get-adjacent game-state %))
       (apply concat)
       dedupe
       (filter #(is-color game-state :empty %))
       count))

(defn remove-stone [game-state coord]
  (assoc-in game-state [:board coord] :empty))

(defn remove-stones [game-state coords]
  (reduce remove-stone game-state coords))

(defn play-move [game-state move]
  (if-not (is-color game-state :empty move)
    game-state
    (let [color (:current-color game-state)
          new-state (-> game-state
                        (assoc-in [:board move] color)
                        switch-player)
          neighbors (get-adjacent game-state move)
          n-other   (filter #(is-color new-state (reverse-color color) %)
                            neighbors)
          n-groups  (map #(get-group new-state %) n-other)
          captured  (filter #(zero? (count-liberties new-state %)) n-groups)
          atari     (some #(= 1 (count-liberties new-state %)) n-groups)]
      (if (and (empty? captured)
               (->> move 
                    (get-group new-state) 
                    (count-liberties new-state) 
                    zero?))
       (-> game-state
         (assoc :in-atari false)
         (assoc :attempted-suicide true))
       (-> new-state
           (remove-stones (apply concat captured))
           (assoc :in-atari atari)
           (assoc :last-move-passed false))))))
