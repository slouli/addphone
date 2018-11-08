(ns addphone.xml.phone
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getPhone
  []
  "TODO: IMPLEMENT GET PHONE METHOD FOR VALIDATION")

(defn phoneBase
  "Common parameters between the phones.  Should not call this method."
  [{:keys [description loc]} & deviceXml]
  {:name "addPhone"                     
   :xml (xml/element :ns:addPhone {:sequence "?"}
          (xml/element :phone {}
            (xml/element :description {} description)
            (xml/element :class {} "Phone")
            (xml/element :protocol {} "SIP")
            (xml/element :protocolSide {} "User")
            (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-Device"))
            (xml/element :devicePoolName {} (str "DP-" loc))
            (xml/element :locationName {} (str "Loc-" loc))
            (xml/element :userLocale {} "English United States")
            (xml/element :ownerUserName {} "americas-lic-user")            
            (xml/element :sipProfileName {} "Standard SIP Profile")            
            (xml/element :commonPhoneConfigName {} "Standard Common Phone Profile")
            (xml/element :enableExtensionMobility {} "true")
            deviceXml))})


(defmulti addPhone :phone)

(defmethod addPhone :CSF
  [{:keys [userId line description loc e164Mask]}]
  (phoneBase {:description description :loc loc}
    (xml/element :name {} (str "CSF" userId))
    (xml/element :product {} "Cisco Unified Client Services Framework")
    (xml/element :mediaResourceListName {} "MRL-CMS")
    (xml/element :phoneTemplateName {} "Standard Client Services Framework")
    (xml/element :securityProfileName {} "Cisco Unified Client Services Framework - Standard SIP Non-Secure Profile")
    (xml/element :lines {}
      (xml/element :line {}
        (xml/element :index {} "1")
        (xml/element :dirn {}
          (xml/element :pattern {} line)
          (xml/element :routePartitionName {} (str "PT-" loc "-Dev")))
        (xml/element :display {} description)
        (xml/element :displayAscii {} description)
        (xml/element :e164Mask {} e164Mask)))))

(defmethod addPhone :IPC
  [{:keys [userId line description loc e164Mask]}]
  (phoneBase {:description description :loc loc}
    (xml/element :name {} (str "SEP" userId))
    (xml/element :product {} "Cisco IP Communicator")
    (xml/element :phoneTemplateName {} "OT 1 Line + Speed Dial - 7945 SIP")
    (xml/element :softkeyTemplateName {} "OT - Standard User - AbbrDial")
    (xml/element :mediaResourceListName {} "MRL-NA")
    (xml/element :securityProfileName {} "Cisco IP Communicator - Standard SIP Non-Secure Profile")))

