(ns manifold-kafka.test.producer
  (:require [manifold.stream :as s]
            [expectations :refer :all]
            [clj-kafka.core :refer [with-resource to-clojure]]
            [clj-kafka.consumer.zk :as zk]
            [manifold-kafka.producer :refer [producer]]
            [manifold-kafka.test.utils :refer [with-test-broker]]
            [clj-kafka.consumer.seq :as zks]))

(def producer-config {"metadata.broker.list" "localhost:9999"
                      "serializer.class"     "kafka.serializer.DefaultEncoder"
                      "partitioner.class"    "kafka.producer.DefaultPartitioner"})

(def test-broker-config {:zookeeper-port 2182
                         :kafka-port     9999
                         :topic          "test"})

(def consumer-config {"zookeeper.connect"  "localhost:2182"
                      "group.id"           "clj-kafka.test.consumer"
                      "auto.offset.reset"  "smallest"
                      "auto.commit.enable" "false"})

(defn string-value
  [k]
  (fn [m]
    (String. (k m) "UTF-8")))

(defn test-message
  []
  (.getBytes "Hello, world"))

(defn send-and-receive
  [message]
  (with-test-broker test-broker-config
                    (with-resource [c (zk/consumer consumer-config)]
                                   zk/shutdown
                                   (with-resource [p (producer producer-config "test")]
                                                  s/close!
                                                  (let [s (zk/stream c "test" 1)]
                                                    (s/put! p message)
                                                    (first (zks/kafka-seq s)))))))

(given (send-and-receive (test-message))
       (expect :topic "test"
               :offset 0
               :partition 0
               (string-value :value) "Hello, world"))
