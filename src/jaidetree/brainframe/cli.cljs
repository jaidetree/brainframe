(ns jaidetree.brainframe.cli
  (:require
   [clojure.pprint :refer [pprint]]
   [promesa.core :as p]
   [jaidetree.env :as env]
   [jaidetree.stream :as stream]
   ["crypto" :as crypto]
   ["fs" :as fs]
   ["fs/promises" :as fsp]
   ["path" :as path]))

(defn fs-events-stream
  [notes-dir]
  (stream/create
   (fn [sink]
     (let [abort-controller (js/AbortController.)]
       (.watch fs notes-dir #js {:recursive true
                                 :signal (.-signal abort-controller)}
               (fn [event-type filepath]
                 (sink
                  {:type (keyword event-type)
                   :filepath (.join path notes-dir filepath)})))
       (fn cleanup []
         (.abort abort-controller))))))

(defn format-event
  [{event-type :type :keys [filepath] :as event}]
  (p/let [stats (-> (.stat fsp filepath)
                    (p/catch (fn [err]
                               (js/console.error err)
                               #js {:size        0
                                    :isDirectory (constantly false)
                                    :isFile      (constantly false)})))]
    (let [size (.-size stats)
          is-directory (.isDirectory stats)
          is-file      (.isFile stats)]
      (pprint [event-type is-directory is-file])
      (case [event-type is-directory is-file]
        [:rename false false] {:type     :file-deleted
                               :filepath filepath
                               :size     size}
        [:rename false true] {:type      :file-created
                              :filepath  filepath
                              :size      size}
        [:change false true] {:type      :file-updated
                              :filepath  filepath
                              :size      size}
        [:rename true false] {:type      :dir-created
                              :filepath  filepath}))))

(defn sha1-str
  [s]
  (-> (.createHash crypto "sha1")
      (.update s)
      (.digest "hex")))

(defn hash-file
  [{:keys [filepath] :as event}]
  (p/let [contents (.readFile fsp filepath #js {:encoding "utf-8"})]
    (assoc event :hash (sha1-str contents))))

(defn file-created?
  [{:keys [prev next]}]
  (and (= (:filepath prev) (:filepath next))
       (= (:type prev) :file-created)
       (= (:type next) :file-updated)))

(defn file-deleted?
  [{:keys [prev next]}]
  (and (= (:filepath prev) (:filepath next))
       (= (:type prev) :file-created)
       (= (:type next) :file-deleted)))

(defn -main
  []
  (let [notes-dir (env/required :BRAINFRAME_NOTES_DIR)]
    (println notes-dir)
    (-> (fs-events-stream notes-dir)
        (.flatMap
         (fn [event]
           (stream/promise
            #(format-event event))))
        (.flatMap
         (fn [event]
           (if (contains? #{:file-created :file-updated} (:type event))
             (stream/promise #(hash-file event))
             (stream/of event))))
        (.scan
         nil
         (fn [prev next]
           {:prev (:next prev)
            :next next}))
        (.flatMap
         (fn [pair]
           (cond
             (file-created? pair)   (stream/never)
             (file-deleted? pair)   (stream/never)
             :else                  (stream/of (:next pair)))))
        (.onValue
         (fn [event]
           (pprint (js->clj event)))))))

(comment
  (sha1-str "test"))
