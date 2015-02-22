(ns manifold-kafka.test.consumer
  (:require [manifold-kafka.consumer :refer [input-stream]]
            [manifold.stream :as s]
            [expectations :refer :all]
            [clj-kafka.core :refer [with-resource to-clojure]]
            [clj-kafka.producer :refer [producer send-messages message]]
            [manifold-kafka.test.utils :refer [with-test-broker]]))

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
  (message "test" (.getBytes "Hello, world")))

(defn send-and-receive
  [messages]
  (with-test-broker test-broker-config
                    (with-resource [stream (input-stream consumer-config "test")]
                                   s/close!
                                   (let [p (producer producer-config)]
                                     (send-messages p messages)
                                     @(s/take! stream 1)))))

(given (send-and-receive [(test-message)])
       (expect :topic "test"
               :offset 0
               :partition 0
               (string-value :value) "Hello, world"))

