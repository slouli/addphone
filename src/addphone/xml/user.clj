(ns addphone.xml.user
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getUser
  [userId]
  {:name "getUser"
   :xml (xml/element :ns:getUser {:sequence "?"}
               (xml/element :userid {} userId))})