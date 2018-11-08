(ns addphone.xml.deviceProfile
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getDeviceProfile
  [dpName]
  {:name "getDeviceProfile"
   :xml (xml/element :ns:getDeviceProfile {:sequence "?"}
               (xml/element :name {} dpName))})

(defn deviceProfileBase
  [{:keys [userId description line loc e164Mask userLocale]} & xml]
  {:name "addDeviceProfile" 
   :xml (xml/element :ns:addDeviceProfile {:sequence "?"}
          (xml/element :deviceProfile {}                       
            (xml/element :name {} userId)
            (xml/element :description {} description)
            (xml/element :userLocale {} userLocale)
            (xml/element :class {} "Device Profile")
            (xml/element :protocol {} "SIP")
            (xml/element :protocolSide {} "User")
            xml
            (xml/element :lines {}
              (xml/element :line {}
                (xml/element :index {} "1")
                (xml/element :dirn {}
                  (xml/element :pattern {} line)
                  (xml/element :routePartitionName {} (str "PT-" loc "-Dev")))
                (xml/element :display {} description)
                (xml/element :displayAscii {} description)
                (xml/element :e164Mask {} e164Mask)))))})


(defmulti addDeviceProfile :deviceProfile)

(defmethod addDeviceProfile :7945
  [{:keys [userId description line loc e164Mask userLocale] :as args}]
  (deviceProfileBase args
    (xml/element :product {} "Cisco 7945")
    (xml/element :phoneTemplateName {} "OT 1 Line + Speed Dial - 7945 SIP")
    (xml/element :softkeyTemplateName {} "OT - Standard User - AbbrDial")))
  

(defmethod addDeviceProfile :8851
  [{:keys [userId description line loc e164Mask userLocale] :as args}]
  (deviceProfileBase args
    (xml/element :product {} "Cisco 8851")
    (xml/element :phoneTemplateName {} "Standard 8851 SIP")
    (xml/element :softkeyTemplateName {} "OT - Standard User - AbbrDial")))
  

