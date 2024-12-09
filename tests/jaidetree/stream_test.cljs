(ns jaidetree.stream-test
  (:require
   [promesa.core :as p]
   [cljs.test :as t :refer [testing is]]
   [jaidetree.testing :refer [deftest-async]]
   [jaidetree.stream :as stream]))

(deftest-async of-to-promise-test
  (-> (stream/of 1)
      (.toPromise)
      (.then
       (fn [x]
         (testing "can wrap stream in promise"
           (is (= x 1)))))))

(deftest-async from-seq-to-promise-test
  (-> (stream/from-seq [2 3 4])
      (.toPromise)
      (.then
       (fn [x]
         (testing "can wrap seq stream in promise"
           (is (= x 4)))))))

(deftest-async from-promise-lazy
  (let [result (atom 0)
        src (-> (stream/from-promise
                 #(p/do
                    (p/delay 0 nil)
                    (swap! result inc)
                    @result))
                (.toPromise)
                (.then
                 (fn [x]
                   (testing "promise is lazily created when stream is consumed"
                     (is (= x 1))))))]
    (testing "promise has not fired yet"
      (is (= @result 0)))
    src))

(deftest-async to-promise-errors-test
  (-> (stream/of (stream/error (js/Error. "Expected failure")))
      (.toPromise)
      (.then identity identity)
      (.then
       (fn [error]
         (testing "A stream error rejects a promise"
           (is (instance? js/Error error))
           (is (= (.-message error) "Expected failure")))))))

(deftest-async promise->stream-test
  (-> (apply
       (stream/promise->stream inc)
       [1])
      (.toPromise)
      (.then
       (fn [x]
         (testing "Promise returned"
           (is (= x 2)))))))

(deftest-async flatmap-promise->stream-test2
  (-> (stream/of 1)
      (.flatMapConcat (stream/promise->stream inc))
      (.toPromise)
      (.then
       (fn [x]
         (testing "Flatmap'd value to a stream from a promise"
           (is (= x 3)))))
      (.catch
       (fn [x]
         (println "promise failed" x)))))
