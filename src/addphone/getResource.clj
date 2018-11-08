
(ns addphone.getResource
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))



(defn getResource
  [filename]
  (->> filename
       io/resource
       slurp
       edn/read-string))

