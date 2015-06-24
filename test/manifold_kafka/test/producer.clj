(ns manifold-kafka.test.producer
  (:require [manifold.stream :as s]
            [midje.sweet :refer :all]
            [clojure.test :refer :all]
            [clj-kafka.core :refer [with-resource to-clojure]]
            [clj-kafka.consumer.zk :as zk]
            [manifold-kafka.producer :refer [producer]]
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

(defn test-message
  []
  (.getBytes "Hello, world"))

(defn send-and-receive
  [msg]
  (with-test-broker test-broker-config
                    (with-resource [c (zk/consumer consumer-config)]
                                   zk/shutdown
                                   (with-resource [p (producer producer-config "test")]
                                                  s/close!
                                                  (let [s (zk/messages c "test")]
                                                    (s/put! p msg)
                                                    (first s))))))

(deftest producer-facts
  (fact "should produce simple message"
        (let [result (send-and-receive (test-message))]
          result => (contains {:topic     "test"
                               :offset    0
                               :partition 0})
          (-> result :value (String. "UTF-8")) => "Hello, world")))
