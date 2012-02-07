(ns circle.util.core
  (:use [arohner.utils :only (inspect)])
  (:import java.security.MessageDigest))

(defn printfln [& args]
  (apply printf args)
  (newline))

(defn apply-map
  "Takes a fn and any number of arguments. Applies the arguments like
  apply, except that the last argument is converted into keyword
  pairs, for functions that keyword arguments.

  (apply foo :a :b {:c 1 :d 2}) => (foo :a :b :c 1 :d 2)"
  [f & args*]
  (let [normal-args (butlast args*)
        m (last args*)]
    (apply f (concat normal-args (flatten (seq m))))))

(defn seq1
  "Converts a normal seq, with chunking behavior, to one-at-a-time. See http://blog.fogus.me/2010/01/22/de-chunkifying-sequences-in-clojure/"
  [#^clojure.lang.ISeq s]
  (reify clojure.lang.ISeq
    (first [_] (.first s))
    (more [_] (seq1 (.more s)))
    (next [_] (let [sn (.next s)] (and sn (seq1 sn))))
    (seq [_] (let [ss (.seq s)] (and ss (seq1 ss))))
    (count [_] (.count s))
    (cons [_ o] (.cons s o))
    (empty [_] (.empty s))
    (equiv [_ o] (.equiv s o))))

(defn conj-vec
  "conjoin, but (conj-vec nil item) returns [item] rather than (item), like normal conj"
  [coll x & xs]
  (apply conj (or coll []) x xs))

(defn apply-if
  "if test (apply f arg args) else arg"
  [test f arg & args]
  (if test
    (apply f arg args)
    arg))

(defn byte-array-to-hex-string [ba]
  (-> ba
      (org.apache.commons.codec.binary.Hex/encodeHex)
      (String.)))

(defn sha1
  "Returns the SHA1. Takes a byte array or string"
  [s]
  (let [s (if (string? s)
            (.getBytes s)
            s)]
    (-> (MessageDigest/getInstance "SHA-1")
        (.digest s)
        (byte-array-to-hex-string))))

(defmacro defn-once
  "Defs a function of no arguments that will execute body only once,
  the first time the function is called. On all future calls, returns
  the cached return value from the first run.

 ex:
  (defn-once foo
     (bar)
     (time/now))"
  [name & body]
  `(do
     (defonce name-delay#
       (delay (do ~@body)))
     (defn ~name []
       @name-delay#)))

(defn re
  "Creates a regex from a string"
  [s]
  (java.util.regex.Pattern/compile s))