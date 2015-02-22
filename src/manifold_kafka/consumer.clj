(ns manifold-kafka.consumer
  (:require [clj-kafka.consumer.zk :as zk]
            [clj-kafka.core :refer [to-clojure]]
            [manifold.stream :as s]))

(defn input-stream
  "takes a kafka consumer config map, a topic and and options map and returns a manifold stream which represents the
  topic.  When the stream is closed, the Kafka Consumer will be shut down."
  [consumer-config topic & {:keys [threads]
                            :or   {threads 1}}]
  (let [stream (s/stream)
        consumer (zk/consumer consumer-config)
        kafka-stream (zk/stream consumer topic threads)]
    (s/on-closed stream (fn [] (zk/shutdown consumer)))
    (future
      (doseq [msg kafka-stream]
        (s/put! stream (to-clojure msg))))
    stream))
