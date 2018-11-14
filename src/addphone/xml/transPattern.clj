(ns addphone.xml.transPattern
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]))


(defn addTransPattern
  [{:keys [pattern dn description pt loc]}]
  {:name "addTransPattern"
   :xml (xml/element :ns:addTansPattern {:sequence "?"}
          (xml/element :transPattern {}
            (xml/element :pattern {} pattern)
            (xml/element :description {} description)
            (xml/element :usage {} "Translation")
            (xml/element :routePartitionName {} pt)
            (xml/element :callingSearchSpace {} (str "CSS-" loc "-Internal"))
            (xml/element :patternUrgency {} "true")
            (xml/element :blockEnable {} "false")
            (xml/elemetn :calledPartyTransformationMask {} dn)))})
