(ns slacker.test.server
  (:use [slacker server serialization common])
  (:use [clojure.test]))

(def funcs {"plus" + "minus" - "prod" * "div" /})
(def params (write-carb [100 0]))

(deftest test-server-pipeline
  (let [server-pipeline (build-server-pipeline funcs identity identity)
        req {:content-type :carb
             :data [params]
             :fname "plus"}
        req2 {:content-type :carb
              :data [params]
              :fname "never-found"}
        req3 {:content-type :carb
              :data [params]
              :fname "div"}]

    (.rewind params)
    (let [result (server-pipeline req)]
      (is (= :success (:code result)))
      (is (= 100 (read-carb (:result result)))))

    (.rewind params)
    (let [result (server-pipeline req2)]
      (is (= :not-found (:code result))))

    (.rewind params)
    (let [result (server-pipeline req3)]
      (is (= :exception (:code result))))))

(def interceptor (fn [req] (update-in req [:result] str)))

(deftest test-server-pipeline-interceptors
  (let [server-pipeline (build-server-pipeline funcs identity interceptor)
        req {:content-type :carb
             :data [params]
             :fname "prod"}]
    (.rewind params)
    (is (= "0" (read-carb (:result (server-pipeline req)))))))

(deftest test-ping
  (let [request [version :type-ping :json nil nil]
        response (handle-request nil request nil)]
    (is (= :type-pong (nth response 1)))))

(deftest test-invalid-packet
  (let [request [version :type-unknown :json nil nil]
        response (handle-request nil request nil)]
    (is (= :type-error (nth response 1)))
    (is (= :invalid-packet (nth response 3)))))

