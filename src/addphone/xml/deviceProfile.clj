(ns addphone.xml.deviceProfile
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getDeviceProfile
  [dpName]
  {:name "getDeviceProfile"
   :xml (xml/element :ns:getDeviceProfile {:sequence "?"}
               (xml/element :name {} dpName))})
     

(defn addDeviceProfile
  [{:keys [userId description line loc model]}]
  (let [models {:8851 "Cisco 8851"
                :7945 "Cisco 7945"}
        pbtemplate {:8851 "Standard 8851 SIP"
                    :7945 "OT 1 Line + Speed Dial - 7945 SIP"}
        sktemplate {:8851 "OT - Standard User - AbbrDial"
                    :7945 "OT - Standard User - AbbrDial"}
        masks {:RichmondHill "90576XXXXX"
               :Toronto "41695XXXXX"}]
    
    {:name "addDeviceProfile" 
     :xml (xml/element :ns:addDeviceProfile {:sequence "?"}
            (xml/element :deviceProfile {}                       
              (xml/element :name {} userId)
              (xml/element :description {} description)
              (xml/element :userLocale {} "English United States")
              (xml/element :product {} ((keyword model) models)) ;;Can be one of "Cisco 7945" "Cisco 8851" "Cisco Unified Services Framework"
              (xml/element :class {} "Device Profile")
              (xml/element :protocol {} "SIP")
              (xml/element :protocolSide {} "User")
              (xml/element :phoneTemplateName {} ((keyword model) pbtemplate))
              (xml/element :softkeyTemplateName {} ((keyword model) sktemplate))
              (xml/element :lines {}
                (xml/element :line {}
                  (xml/element :index {} "1")
                  (xml/element :dirn {}
                    (xml/element :pattern {} line)
                    (xml/element :routePartitionName {} (str "PT-" loc "-Dev")))
                  (xml/element :display {} description)
                  (xml/element :displayAscii {} description)
                  (xml/element :e164Mask {} ((keyword loc) masks))))))}))

