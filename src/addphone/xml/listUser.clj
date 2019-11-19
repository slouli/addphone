(ns addphone.xml.listUser
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]
            [addphone.xml.parseAxlResponse :as parse]
            [clojure.string :as str]
            [clojure.data.zip.xml :as zip-xml]
            [addphone.utilities.zip :refer [zipify]]))

(defn listUser
  [{:keys [userId]} & userXml]
  {:name "listUser"
   :xml (xml/element :ns:listUser {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :userid {} userId))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :userid {})
            (xml/element :firstName {})
            (xml/element :lastName {})
            (xml/element :homeCluster {})
            (xml/element :status {})
            (xml/element :primaryExtension {}
              (xml/element :pattern {})
              (xml/element :routePartitionName {}))))})

(defn listUserSql
  "Users that are not LDAP Synced OR have an IPCC config"
  [& userXml]
  {:name "executeSQLQuery"
   :xml (xml/element :ns:executeSQLQuery {}
          (xml/element :sql {} 
            "
            SELECT u.userid 
            FROM endusernumplanmap as unmap 
            RIGHT JOIN enduser as u ON unmap.fkenduser = u.pkid 
            WHERE unmap.tkdnusage = '2' OR u.fkdirectorypluginconfig IS NULL
            "))})


(defn parseListUserSql
  "Returns a set of the userids that will not get Jabber accounts"
  [xmlResp]
  (let [zipper (zipify xmlResp)
        userList (zip-xml/xml-> zipper :Envelope :Body :executeSQLQueryResponse
                   :return :row :userid zip-xml/text)]
    (into #{} (map keyword userList))))

(defn parseListUsers
  [xmlResp]
  (let [zipper (zipify xmlResp)
        userids (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                  :return :user :userid zip-xml/text)
        homecluster (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                     :return :user :homeCluster zip-xml/text)
        status (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                     :return :user :status zip-xml/text)
        firstname (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                   :return :user :firstName zip-xml/text)
        lastname (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                  :return :user :lastName zip-xml/text)
        primarydn (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                    :return :user :primaryExtension :pattern zip-xml/text)
        primarypt (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                    :return :user :primaryExtension :routePartitionName zip-xml/text)
        userVector (map vector userids firstname lastname homecluster status primarydn primarypt)
        filteredUserVector (filter #(and (= "true" (% 3)) (="1" (% 4))) userVector)]
    (zipmap
      (map #(keyword (% 0)) filteredUserVector)
      filteredUserVector)))
    
(defn listUserGeneral
  [{:keys [userIdPrefix]} & userXml]
  {:name "listUser"
   :xml (xml/element :ns:listUser {:sequence "?"}
          (xml/element :searchCriteria {}
            (xml/element :userid {} (str userIdPrefix "%")))
          (xml/element :returnedTags {:uuid "?"}
            (xml/element :userid {})
            (xml/element :firstName {})
            (xml/element :lastName {})
            (xml/element :directoryUri)
            (xml/element :homeCluster {})
            (xml/element :telephoneNumber {})))
   :parse (fn [xml] 
            (let [zipper (zipify xml)
                  userids (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                            :return :user :userid zip-xml/text)
                  homeclusters (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                                 :return :user :homeCluster zip-xml/text)
                  firstnames (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                               :return :user :firstName zip-xml/text)
                  lastnames (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                              :return :user :lastName zip-xml/text)
                  directoryUris (zip-xml/xml-> zipper :Envelope :Body :listUserResponse
                                  :return :user :directoryUri zip-xml/text)]
              (set (map vector userids homeclusters firstnames lastnames directoryUris))))})
                  
  
