(ns addphone.xml.updateUser
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))

(defn parseUpdateUser
  [xmlResp]
  (let [zipper (zipify xmlResp)
        error (zip-xml/xml1-> zipper :Envelope :Body :Fault
                :faultstring zip-xml/text)
        success (zip-xml/xml1-> zipper :Envelope :Body :updateUserResponse
                  :return zip-xml/text)]
    (str "ERR: " error "\n" "Success: " success "\n\n")))

(defn updateUser
  ([userid] (updateUser userid '()))
  ([userid deviceList]
   {:name "updateUser"
    :xml (xml/element :ns:updateUser {:sequence "?"}
           (xml/element :userid {} userid)
           (xml/element :serviceProfile {} "Cisco Jabber Service NA")
           ;;(xml/element :serviceProfile {} "Cisco Jabber Service EMEA")
           ;;(xml/element :serviceProfile {} "Cisco Jabber Service APAC")
           (xml/element :homeCluster {} "true")
           (xml/element :associatedGroups {}
             (xml/element :userGroup {}
               (xml/element :name {} "Standard CCM End Users"))
             (xml/element :userGroup {}
               (xml/element :name {} "Standard CTI Enabled")))
           (xml/element :associatedDevices {}
             (map
               #(xml/element :device {} %) (conj deviceList (str "CSF" userid)))))}))
