(ns addphone.xml.deviceProfile
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getDeviceProfile
  [dpName]
  {:name "getDeviceProfile"
   :xml (xml/element :ns:getDeviceProfile {:sequence "?"}
               (xml/element :name {} dpName))})
