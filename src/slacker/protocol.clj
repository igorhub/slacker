(ns slacker.protocol
  (:refer-clojure :exclude [byte float double])
  (:use [link.codec]))

(def
  ^{:doc "Protocol version."}
  version (short 5))

(def packet-type
  (enum (byte) {:type-request 0
                :type-response 1
                :type-ping 2
                :type-pong 3
                :type-error 4
                :type-auth-req 5
                :type-auth-ack 6
                :type-inspect-req 7
                :type-inspect-ack 8
                :type-interrupt 9}))

(def content-type
  (enum (byte) {:carb 0 :json 1 :clj 2 :nippy 3
                :deflate-carb 10
                :deflate-json 11
                :deflate-clj 12
                :deflate-nippy 13}))

(def result-codes
  (enum (byte) {:success 0
                :not-found 11
                :exception 12
                :protocol-mismatch 20
                :invalid-packet 21
                :acl-rejct 22}))

;; :type-request
(def slacker-request-codec
  (frame
   content-type
   (string :encoding :utf-8 :prefix (uint16))
   (byte-block :prefix (uint32))))

;; :type-response
(def slacker-response-codec
  (frame
   content-type
   result-codes
   (byte-block :prefix (uint32))))


;; :type-ping
(def slacker-ping-codec
  (frame))

;; :type-pong
(def slacker-pong-codec
  (frame))

;; :type-error
(def slacker-error-codec
  (frame
   result-codes))

;; :type-auth-req
(def slacker-auth-req-codec
  (frame
   (string :encoding :ascii :prefix (uint16))))

;; type-auth-ack
(def slacker-auth-ack-codec
  (frame
   (enum (byte) {:auth-ok 0
                 :auth-reject 1})))

;; type-inspect-req
(def slacker-inspect-req-codec
  (frame
   (enum (byte) {:functions 0
                 :meta 1})
   (string :prefix (uint16) :encoding :utf-8)))

;; type-inspect-ack
(def slacker-inspect-ack-codec
  (frame
   (string :encoding :utf-8 :prefix (uint16))))

;; type-interrupt
(def slacker-interrupt-codec
  (frame
   (int32)))

(def slacker-base-codec
  (frame
   (byte) ;; protocol version
   (int32) ;; transaction id
   (header
    packet-type
    {:type-request slacker-request-codec
     :type-response slacker-response-codec
     :type-ping slacker-ping-codec
     :type-pong slacker-pong-codec
     :type-error slacker-error-codec
     :type-auth-req slacker-auth-req-codec
     :type-auth-ack slacker-auth-ack-codec
     :type-inspect-req slacker-inspect-req-codec
     :type-inspect-ack slacker-inspect-ack-codec
     :type-interrupt slacker-interrupt-codec})))
