(ns addphone.xml.user
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]))

(defn getUser
  [userId]
  {:name "getUser"
   :xml (xml/element :ns:getUser {:sequence "?"}
               (xml/element :userid {} userId))})

(defn userBase
  [{:keys [userId description line loc userLocale]} & userXml]
  {:name "addUser"
   :xml (xml/element :ns:addUser {:sequence "?"}
          (xml/element :user {}
            (xml/element :userid {} userId)
            (xml/element :firstName {} (first (str/split description #" ")))
            (xml/element :lastName {} (str/join (rest (str/split description #" "))))
            (xml/element :pin {} line)
            (xml/element :mailid {} (str userId "@opentext.com"))
            (xml/element :telephoneNumber {} (str "*1" line))
            (xml/element :userLocale {} userLocale)
            (xml/element :homeCluster {} "true")
            (xml/element :subscribeCallingSearchSpaceName {} "CSS-Presence")
            userXml
            (xml/element :associatedGroups {}
              (xml/element :userGroup {} 
                (xml/element :name {} "Standard CCM End Users")))))})

(defmulti addUser :phone)

(defmethod addUser :CSF
  [{:keys [userId line loc userLocale] :as args}]
  (userBase args
    (xml/element :associatedDevices {}
     (xml/element :device {} (str "CSF" userId)))))

(defmethod addUser :IPC
  [{:keys [userId line loc userLocale] :as args}]
  (userBase args
    (xml/element :phoneProfiles {}
      (xml/element :profileName {} userId))
    (xml/element :defaultProfile {} userId)))

(defn updateUser
  [{:keys [userId line loc]}]
  {:name "updateUser"
   :xml (xml/element :ns:updateUser {:sequence "?"}
          (xml/element :userid {} userId)
          (xml/element :primaryExtension {}
            (xml/element :pattern {} line)
            (xml/element :routePartitionName {} (str "PT-" loc "-Dev"))))})      
