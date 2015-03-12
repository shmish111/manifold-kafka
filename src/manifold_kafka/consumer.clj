(ns manifold-kafka.consumer
  (:require [clj-kafka.consumer.zk :as zk]
            [clj-kafka.core :refer [to-clojure]]
            [manifold.stream :as s]
            [cheshire.core :refer [parse-string]]))

(defn parse
  [msg]
  (update-in msg [:value] #(parse-string (String. % "UTF-8") true)))

(defn input-stream
  "takes a kafka consumer config map, a topic and and options map and returns a manifold stream which represents the
  topic.  When the stream is closed, the Kafka Consumer will be shut down."
  [consumer-config topic & {:keys [threads deserializer]
                            :or   {threads 1}}]
  (let [input (s/stream)
        output (s/stream)
        consumer (zk/consumer consumer-config)
        kafka-stream (zk/stream consumer topic threads)]
    (cond (= :json deserializer) (s/connect-via input #(s/put! output (parse %)) output)
          :else (s/connect input output))
    (s/on-closed output (fn [] (zk/shutdown consumer)))
    (future
      (doseq [msg kafka-stream]
        (s/put! input (to-clojure msg))))
    output))
