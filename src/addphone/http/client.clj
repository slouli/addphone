(ns addphone.http.client
  (:require [org.httpkit.client :as http]
            [addphone.http.creds :as creds]
            [clojure.data.xml :as xml]
            [addphone.xml.soapBase :as soap]))

(defn axl
  [{:keys [ip ver]} method xml]
  (http/post (str "https://" ip ":8443/axl/")
             {:basic-auth creds/creds
              :headers    {"Content-Type" "text/xml"
                           "SOAPAction"   (str "CUCM:DB ver=" ver " " method)}
              :body       (cond
                            (string? xml) xml
                            :else (xml/emit-str (soap/soapBase ver xml)))
              :insecure?  true}
             (fn [{:keys [status headers body error]}]
               (str body error))))

(defn ris
  [{:keys [ip ver xml]}]
  (let
    [link (cond
            (= (str ver) "8.5") (str "https://" ip ":8443/realtimeservice/services/RisPort70")
            (= (str ver) "10.5") (str "https://" ip ":8443/realtimeservice2/services/RISService70"))
     body (cond
            (string? xml) xml
            :else (xml/emit-str xml))]
    (http/post link
               {:basic-auth creds/creds
                :headers    {"Content-Type" "text/xml"
                             "SOAPAction"   "http://schemas.cisco.com/ast/soap/action/#RisPort70#SelectCmDevice"}
                :body       body
                :insecure?  true}
               (fn [{:keys [status headers body error]}]
                 (str body)))))
