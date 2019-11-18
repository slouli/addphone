(ns addphone.xml.addPhone
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

(defn addPhone
  "Common parameters between the phones.  Should not call this method."
  [{:keys [name description devicePool css location userLocale networkLocale lines]}]
  {:name "addPhone"                     
   :xml (xml/element :ns:addPhone {:sequence "?"}
           (xml/element :phone {}
             (xml/element :name {} (str "CSF" name))
             (xml/element :product {} "Cisco Unified Client Services Framework")
             (xml/element :phoneTemplateName {} "Standard Client Services Framework")
             (xml/element :securityProfileName {} "Cisco Unified Client Services Framework - Standard SIP Non-Secure Profile")
             (xml/element :mediaResourceListName {} "MRL-CMS")
             (xml/element :description {} (str "Cisco Jabber " description))
             (xml/element :class {} "Phone")
             (xml/element :protocol {} "SIP")
             (xml/element :protocolSide {} "User")
             (xml/element :callingSearchSpaceName {} css)
             (xml/element :devicePoolName {} devicePool)
             (xml/element :locationName {} location)
             (xml/element :userLocale {} userLocale)
             (xml/element :networkLocale {} networkLocale)
             (xml/element :ownerUserName {} "svc_ucapac_lic")
             ;;(xml/element :ownerUserName {} "svc_ucemea_lic")
             ;;(xml/element :ownerUserName {} "americas-lic-user")            
             (xml/element :sipProfileName {} "Standard SIP Profile For TelePresence Endpoint")            
             (xml/element :commonPhoneConfigName {} "Standard Common Phone Profile")
             (xml/element :enableExtensionMobility {} "false")
             (xml/element :lines {}
               (map createLine lines))))})
  
  
