(ns addphone.xml.listDeviceNumplanMap
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))


(defn listDeviceNumplanMap
  [& userXml]
  {:name "executeSQLQuery"
   :xml (xml/element :ns:executeSQLQuery {}
          (xml/element :sql {} 
            "
            SELECT fkdevice, fknumplan, e164mask
            FROM devicenumplanmap
            "))})
