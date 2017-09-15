(ns livecloud.pie)


(defn polar->cartesian [cx cy radius angle-deg]
  (let [rad (/ (* Math/PI (- angle-deg 90)) 180.0)]
    [(+ cx (* radius (Math/cos rad)))
     (+ cy (* radius (Math/sin rad)))]))

(def +colors+ ["#468966" "#FFF0A5" "#FFB03B" "#B64926" "#8E2800"])

(defn pie [{:keys [width height radius show-text show-legend]} items]
  (let [cx (/ width 2)
        cy (+ radius 5)
        radius (or radius (- (/ width 2) 6))
        total (reduce + 0 (vals items))
        all-items (seq items)]
    (loop [slices (list)
           angle 0
           [[label count] & items] all-items
           colors (cycle +colors+)]
      (if-not label
        [:span.pie
         (when show-legend
           [:div.pie-legend
            (map (fn [l c]
                   [:div.pie-legend-item
                    [:div.pie-legend-color {:style {:background-color c}}]
                    l])
                 (map first all-items)
                 (cycle +colors+))])
         [:svg {:xmlns  "http://www.w3.org/2000/svg"
                :width  width
                :height height}
          slices]]

        (let [slice-angle (* 360 (/ count total))
              start-angle angle
              end-angle (+ angle slice-angle)
              large? (if (< (- end-angle start-angle) 180) 0 1)
              [sx sy] (polar->cartesian cx cy radius start-angle)
              [ex ey] (polar->cartesian cx cy radius end-angle)
              [tx ty] (polar->cartesian cx cy (* 0.55 radius) (+ start-angle (/ slice-angle 2)))]
          (recur (conj slices
                       ^{:key label}
                       [:g
                        (if (= 360 slice-angle)
                          [:circle {:cx           cx :cy cy :r radius
                                    :fill         (first colors)
                                    :stroke       "black"
                                    :stroke-width 1}]
                          [:path {:d            (str "M" cx " " cy " "
                                                     "L" sx " " sy " "
                                                     "A" radius " " radius " 0 " large? " 1 " ex " " ey
                                                     "L" cx " " cy)

                                  :fill         (first colors)
                                  :stroke       "black"
                                  :stroke-width 1}])
                        (cond
                          (= show-text :percent)
                          [:text {:x tx :y ty :text-anchor "middle"}
                           (str (.toFixed (* 100 (/ count total)) 1) "%")]


                          show-text
                          [:text {:x         tx :y ty :text-anchor "middle"
                                  :transform (str "rotate( " (let [a (+ start-angle (/ slice-angle 2))]
                                                               (if (and (> a 90) (< a 270))
                                                                 (- a 180) a))
                                                  " " tx "," ty ")")}
                           label]

                          :default nil)])
                 (+ angle slice-angle)
                 items
                 (rest colors)))))))
