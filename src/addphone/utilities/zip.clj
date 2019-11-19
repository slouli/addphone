(ns addphone.utilities.zip
  (:require [clojure.data.xml :as xml]
            [clojure.zip :as zip]))

;Takes XML as input and returns a zipper
;Used to facilitate parsing of XML
(defn zipify
  [xmlResponse]
  (zip/xml-zip (xml/parse-str xmlResponse)))
