(ns manifold-kafka.test.consumer
  (:require [manifold-kafka.consumer :refer [input-stream]]
            [manifold.stream :as s]
            [midje.sweet :refer :all]
            [clj-kafka.core :refer [with-resource to-clojure]]
            [clj-kafka.producer :as p :refer [producer send-messages]]
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
  (p/message "test" (.getBytes "{\"yo\": \"Hello, world\"}")))

(defn send-and-receive
  [messages]
  (with-test-broker test-broker-config
                    (with-resource [stream (input-stream consumer-config "test")]
                                   s/close!
                                   (let [p (producer producer-config)]
                                     (send-messages p messages)
                                     (deref (s/take! stream 1) 1000 nil)))))

(fact "should consume simple message"
      (let [result (send-and-receive [(test-message)])]
        result => (contains {:topic     "test"
                             :offset    0
                             :partition 0})
        (-> result :value (String. "UTF-8")) => "{\"yo\": \"Hello, world\"}"))

(defn send-and-receive-json
  [messages]
  (with-test-broker test-broker-config
                    (with-resource [stream (input-stream consumer-config "test" :deserializer :json)]
                                   s/close!
                                   (let [p (producer producer-config)]
                                     (send-messages p messages)
                                     (deref (s/take! stream 1) 1000 nil)))))

(fact "should consume json message"
      (send-and-receive-json [(test-message)]) => (contains {:topic     "test"
                                                             :offset    0
                                                             :partition 0
                                                             :value     {:yo "Hello, world"}}))