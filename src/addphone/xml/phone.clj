(ns addphone.xml.phone
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))


(defn addPhone
  [name description loc line pt e164Mask]
  {:name "addPhone"
   :xml (xml/element :ns:addPhone {:sequence "?"}
          (xml/element :phone {} 
            (xml/element :name {} name)
            (xml/element :description {} description)
            (xml/element :protocol {} "SIP")
            (xml/element :protocolSide {} "User")
            (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-Device"))
            (xml/element :devicePoolName {} (str "DP-" loc))
            (xml/element :locationName {} (str "Loc-" loc))
            (xml/element :userLocale {} "English United States")
            (xml/element :ownerUserName {} "americas-lic-user")
            (xml/element :mediaResourceListName {} "MRL-CMS")
            (xml/element :securityProfileName {} "Cisco Unified Client Services Framework - Standard SIP Non-Secure Profile")
            (Xml/element :sipProfileName {} "Standard SIP Profile")
            (xml/element :phoneTemplateName {} "Standard Client Services Framework")
            (xml/element :commonPhoneConfigName {} "Standard Common Phone Profile")
            (xml/element :lines {}
              (xml/element :line {}
                (xml/element :dirn {}
                  (xml/element :pattern {} line)
                  (xml/element :routePartitionName {} pt))
                (xml/element :display {} description)
                (xml/element :displayAscii {} description)
                (xml/element :e164Mask {} e164Mask)))))})
