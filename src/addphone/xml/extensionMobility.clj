(ns addphone.xml.extensionMobility
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]))

(defn doDeviceLogin
  "Function to permanently log users in with EM"
  [deviceName userId]
  {:name "doDeviceLogin"
   :xml (xml/element :ns:doDeviceLogin {:sequence "?"}
          (xml/element :deviceName {} deviceName)
          (xml/element :loginDuration {} "0")
          (xml/element :profileName {} userId)
          (xml/element :userId {} userId))})

(defn doDeviceLogout
  "Function to log a user out of a device "
  [deviceName]
  {:name "doDeviceLogout"
   :xml (xml/element :ns:doDeviceLogout {:sequence "?"}
          (xml/element :deviceName {} deviceName))})
