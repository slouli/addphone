(ns addphone.xml.updateAppUser
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))


(defn updateAppUser
  [userid & devices]
  {:name "updateAppUser"                     
   :xml (xml/element :ns:updateAppUser {:sequence "?"}
          (xml/element :userid {} userid)
          (xml/element :associatedDevices {}
            (map #(xml/element :device {} %) devices)))})
