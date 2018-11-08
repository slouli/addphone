(ns addphone.xml.user
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]))

(defn getUser
  [userId]
  {:name "getUser"
   :xml (xml/element :ns:getUser {:sequence "?"}
               (xml/element :userid {} userId))})

(defn addUser
  [{:keys [userId line loc userLocale]}]
  {:name "addUser"
   :xml (xml/element :ns:addUser {:sequence "?"}
          (xml/element :user {}
            (xml/element :userid {} userId)
            (xml/element :lastName {} userId) ;;lastname doesn't matter.  It just needs to be there until AD sync happens.
            (xml/element :pin {} line)
            (xml/element :mailid {} (str userId "@opentext.com"))
            (xml/element :telephoneNumber {} (str "*1" line))
            (xml/element :userLocale {} userLocale)
            (xml/element :homeCluster {} "true")
            (xml/element :associatedDevices {}
              (xml/element :device {} (str "CSF" userId)))
            (xml/element :phoneProfiles {} 
              (xml/element :profileName {} userId))
            (xml/element :defaultProfile {} userId)
            (xml/element :subscribeCallingSearchSpaceName {} "CSS-Presence")
            (xml/element :associatedGroups {}
              (xml/element :userGroup {} 
                (xml/element :name {} "Standard CCM End Users")))))})

(defn updateUser
  [{:keys [userId line loc]}]
  {:name "updateUser"
   :xml (xml/element :ns:updateUser {:sequence "?"}
          (xml/element :userid {} userId)
          (xml/element :primaryExtension {}
              (xml/element :pattern {} line)
              (xml/element :routePartitionName {} (str "PT-" loc "-Dev"))))})
               
              
            
