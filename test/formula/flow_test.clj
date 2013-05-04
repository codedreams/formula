(ns formula.flow-test
  (:use clojure.test
        formula.core
        formula.validation
        midje.sweet
        hiccup.core
        hiccup.def)
  (:require [midje.util :refer [testable-privates]]
            [clojure.pprint :as p]))


;; (facts "form should have proper display"
;;        (fact "should display correct form display - no errors"
;;              (fform {:class "form-horizontal"} [:post "/f"]
;;                     [[:text :username {:placeholder "Username" :value nil
;;                                        :wrap :div.control-label}]
;;                      [:email :email {:placeholder "Email" 
;;                                      :wrap :div.control-group}]
;;                      [:password :password {:placeholder "Password" 
;;                                            :wrap :div.control-label}]
;;                      [:submit :submit {:value "Submit" :class "btn"}]]
;;                     (conj {} {:wrap-both :div.control-group
;;                               :wrap-all :div
;;                               :wrap-errors :div.controls}))
;;              =>[:form {:class "form-horizontal" :method "POST"
;;                        :action (java.net.URI. "/f")}
;;                 [:div.control-group
;;                  [:div.control-label [:input {:placeholder "Username"
;;                                               :type :text :name "username"
;;                                               :id "username" :value nil}]]]
;;                 [:div.control-group
;;                  [:div.control-group [:input {:placeholder "Email" :type :email
;;                                               :name "email" :id "email" :value nil}]]]
;;                 [:div.control-group
;;                  [:div.control-label [:input {:placeholder "Password" :type :password
;;                                               :name "password" :id "password"
;;                                               :value nil}]]]
;;                 [:div.control-group
;;                  [:input {:class "btn" :type :submit :name "submit" :id "submit"
;;                           :value "Submit"}]]])

;;        (println (html (fform {:class "form-horizontal"} [:post "/f"]
;;                              [[:text :username {:placeholder "Username" :value nil
;;                                                 :wrap :div.control-label}]
;;                               [:email :email {:placeholder "Email" 
;;                                               :wrap :div.control-group}]
;;                               [:password :password {:placeholder "Password" 
;;                                                     :wrap :div.control-label}]
;;                               [:submit :submit {:value "Submit" :class "btn"}]]
;;                              (conj {} {:wrap-outer :div.control-group
;;                                        :wrap-errors :div.controls}))))
