(ns jaidetree.fizzbuzz)

(defn -main
  [& _args]
  (->> (iterate inc 0)
       (take 100)
       (map
        (fn [x]
          (let [by-3 (zero? (mod x 3))
                by-5 (zero? (mod x 5))]
            (when (or by-3 by-5)
              (js/process.stdout.write (str x " ")))
            (when by-3
              (js/process.stdout.write "fizz"))
            (when by-5
              (js/process.stdout.write "buzz"))
            (when (or by-3 by-5)
              (println)))))
       (dorun)))
