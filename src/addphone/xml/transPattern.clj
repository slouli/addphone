(ns addphone.xml.transPattern
  (:require [clojure.data.xml :as xml]
            [addphone.http.client :as client]))


(defn addTransPattern
  [pattern dn description pt loc]
  {:name "addTransPattern"
   :xml (xml/element :ns:addTransPattern {:sequence "?"}
          (xml/element :transPattern {}
            (xml/element :pattern {} pattern)
            (xml/element :description {} description)
            (xml/element :usage {} "Translation")
            (xml/element :routePartitionName {} pt)
            (xml/element :callingSearchSpaceName {} (str "CSS-" loc "-Internal"))
            (xml/element :patternUrgency {} "true")
            (xml/element :blockEnable {} "false")
            (xml/element :useCallingPartyPhoneMask {} "On")
            (xml/element :calledPartyTransformationMask {} dn)))})


(defn updateTransPattern
  [pattern pt dn]
  {:name "updateTransPattern"
   :xml (xml/element :ns:updateTransPattern {:sequence "?"}
          (xml/element :pattern {} pattern)
          (xml/element :routePartitionName {} pt)
          (xml/element :calledPartyTransformationMask {} dn))})
