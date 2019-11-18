(ns addphone.xml.updateDeviceProfile
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn createLine
  "Takes Tuple of the format [index pattern routepartition display e164Mask]"
  [lineTuple]
  (xml/element :line {}
    (xml/element :index {} (lineTuple 0))
    (xml/element :dirn {}
      (xml/element :pattern {} (lineTuple 1))
      (xml/element :routePartitionName {} (lineTuple 2)))
    (xml/element :display {} (lineTuple 3)) 
    (xml/element :displayAscii {} (lineTuple 3))
    (xml/element :e164Mask {} (lineTuple 4))))

(defn updateDeviceProfile
  [name lines]
  {:name "updateDeviceProfile"                     
   :xml (xml/element :ns:updateDeviceProfile {:sequence "?"}
          (xml/element :name {} name)
          (xml/element :lines {}
            (map createLine lines)))})
