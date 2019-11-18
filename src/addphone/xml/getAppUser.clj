(ns addphone.xml.getAppUser
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.zip :as zip]))

(defn zipify
  [resp]
  (zip/xml-zip (xml/parse-str resp)))

(defn parseGetAppUser
  [xmlResp]
  (let [zipper (zipify xmlResp)
        name (zip-xml/xml1-> zipper :Envelope :Body :getAppUserResponse
               :return :appUser :userid zip-xml/text)
        deviceList (zip-xml/xml-> zipper :Envelope :Body :getAppUserResponse
                     :return :appUser :associatedDevices :device zip-xml/text)]
    deviceList))

(defn getAppUser
  [userid]
  {:name "getAppUser"
   :xml (xml/element :ns:getAppUser {:sequence "?"}
          (xml/element :userid {} userid)
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :userid {})
            (xml/element :associatedDevices {}
              (xml/element :device {}))))})

