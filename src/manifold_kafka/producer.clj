(ns manifold-kafka.producer
  (:require [clj-kafka.producer :as cp]
            [manifold.stream :as s]
            [cheshire.core :refer [generate-string]]))

(defn- m->msg
  [topic {:keys [key val] :as msg}]
  (if (and key val)
    (cp/message topic key val)
    (cp/message topic msg)))

(defn- serialize
  [msg serializer]
  (cond (= :json serializer) (-> msg generate-string .getBytes)
        :else msg))

(defn producer
  "takes a kafka producer config map and a topic and returns a manifold stream representing the kafka topic
  producer-config properties are those of kafka.producer.ProducerConfig
  Put a message on the stream to put it on the Kafka topic
  If a message has both :key and :val then the message will be put onto kafka using that key and val,
  otherwise the whole message will be used.
  Optionally you can provide a serializer type, currently the only option is :json
  Closing the stream will close the Kafka Producer.
  See tests for more examples."
  [producer-config topic & {:keys [serializer]}]
  (let [kafka-producer (cp/producer producer-config)
        producer-stream (s/stream)]
    (s/on-closed producer-stream #(.close kafka-producer))
    (s/consume #(cp/send-message kafka-producer (m->msg topic (serialize % serializer))) producer-stream)
    producer-stream))