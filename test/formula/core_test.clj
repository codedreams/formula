(ns formula.core-test
  (:use clojure.test
        formula.core
        midje.sweet
        hiccup.core)
  (:require [midje.util :refer [testable-privates]]))

(testable-privates formula.core input-field)

(facts "input-field - returns a vector input with custom type name and attrs"
       (fact "should return vector with no value"
             (input-field "text" "username" {:class "user"})
             => [:input {:type "text" :name "username" :id "username"
                         :value "" :class "user"}])
       (fact "return vector with value attr"
             (input-field "password" "password" {:value "foobar"})
             => [:input {:type "password" :name "password" :id "password"
                         :value "foobar"}])
       (fact "return vector with extra attr, placeholder"
             (input-field "text" "search" {:placeholder "Search Now"})
             => [:input {:type "text" :name "search" :id "search"
                         :value "" :placeholder "Search Now"}])
       (fact "values should be escaped"
             (input-field "text" "search" {:value "<i>hi</i>"})
             =>[:input {:type "text" :name "search" :id "search"
                         :value "&lt;i&gt;hi&lt;&#x2F;i&gt;"}])
       (fact "should create button tags"
             (input-field "button" "submit" {:name "" :value "Submit"})
             => [:input {:type "button" :name "" :id "submit" :value "Submit"}]))

(testable-privates formula.core display-error)

(facts "display error - should return field with error or just field"
       (fact "should just return field if no error message"
             (display-error :username [:input {:type "text" :name "username"
                                               :value ""}] {})
             => [:input {:type "text" :name "username" :value ""}])
       (fact "should return error and field if error"
             (display-error :username [:input {:type "text" :name "username"
                                               :value ""}]
                            {:username "username must be present"})
             => [:div [:p {:class "username-error"} "username must be present"]
                 [:input {:type "text" :name "username" :value ""}]])
       (fact "should not return error if error for another field"
             (display-error :username [:input {:type "text" :name "username"
                                               :value ""}]
                            {:name "name must be present"})
             => [:input {:type "text" :name "username" :value ""}]))

(facts "generic-input - should be used to produce text, password, email
                       checkbox, radio, hidden, and file fields"
      (fact "should create text field"
            (generic-input :text :search {})
            => [:input {:type :text :name "search" :id "search" :value ""}])
      (fact "should create password field"
            (generic-input :password :search {})
            => [:input {:type :password :name "search" :id "search" :value ""}])
      (fact "should create email field"
            (generic-input :email :search {})
            => [:input {:type :email :name "search" :id "search" :value ""}])
      (fact "should create checkbox field"
            (generic-input :checkbox :search {})
            => [:input {:type :checkbox :name "search" :id "search" :value ""}])
      (fact "should create radio field"
            (generic-input :radio :search {})
            => [:input {:type :radio :name "search" :id "search" :value ""}])
      (fact "should create hidden field"
            (generic-input :hidden :search {})
            => [:input {:type :hidden :name "search" :id "search" :value ""}])
      (fact "should create file field"
            (generic-input :file :search {})
            => [:input {:type :file :name "search" :id "search" :value ""}])
      (fact "should accept extra attributes"
            (generic-input :text :search {:placeholder "Search" :value "Anything"})
            => [:input {:type :text :name "search" :id "search" 
                        :placeholder "Search" :value "Anything"}])
      (fact "should display errors if available"
            (generic-input :text :search {} {:search "File must be jpg"})
            => [:div [:p {:class "search-error"} "File must be jpg"]
                [:input {:type :text :name "search" :id "search" :value ""}]]))

(facts "text-area - should produce a text area field"
       (fact "should create text area with no error"
             (text-area nil :problems {}) => [:textarea {:name :problems
                                                         :id :problems} ""])
       (fact "should create text area with errors"
             (text-area nil :help {} {:help "help must be present"})
             => [:div [:p {:class "help-error"} "help must be present"]
                 [:textarea {:name :help :id :help} ""]])
       (fact "should accept extra attributes"
             (text-area nil :help {:placeholder "Questions Here"
                                   :value "<i>hello</i>"})
             => [:textarea {:name :help :id :help :placeholder "Questions Here"}
                 "&lt;i&gt;hello&lt;&#x2F;i&gt;"]))

(facts "button - should return button element"
       (fact "should return button"
             (button nil :submit {}) => [:button {:type "button" :id :submit} nil])
       (fact "should accept extra attributes"
             (button nil :submit {:class "user button"}) =>
             [:button {:type "button" :id :submit :class "user button"} nil]))

(facts "drop-down - should return drop-down element"
      (fact "should return dropdown"
            (drop-down nil :friends {:options ["so" "say"]})
            => [:select {:name :friends :id :friends}
                '([:option {:selected false} "so"]
                   [:option {:selected false} "say"])])

      (fact "should return error if present"
            (drop-down nil :friends
                       {:options ["so" "say"]} {:friends "not an option"})
            => [:div [:p {:class "friends-error"} "not an option"]
                      [:select {:name :friends :id :friends}
                       '([:option {:selected false} "so"]
                           [:option {:selected false} "say"])]])
      
      (fact "should return dropdown with select attribute"
            (drop-down nil :friends {:options ["so" "say"] :selected "say"})
            => [:select {:name :friends :id :friends}
                '([:option {:selected false} "so"]
                   [:option {:selected true} "say"])]))

(facts "field-map - should have correct functions"
       (fact "should have correct keys"
             (every? #{:textarea :dropdown :button-tag} (keys field-map)) => true)
       (fact "should have 3 key value pairs"
             (count field-map) => 3))



(facts "Form fields - should call field functions"
       (fact "should return fields"
             (form-fields [[:text :username {:class "user" :value "<i>"}]
                           [:password :password]]
                          {:username "must be present" :password "doesn't match"})
             => '([:div
                   [:p {:class "username-error"} "must be present"]
                   [:input {:type :text :class "user" :value "&lt;i&gt;"
                            :id "username" :name "username"}]]
                    [:div
                     [:p {:class "password-error"} "doesn't match"]
                     [:input {:type :password :id "password"
                              :name "password" :value ""}]])))

(facts "fform - should create forms"
       (fact "should create post form"
             (fform [:post "/login"]
                    [[:textarea :username {:class "usern" :value "<i>"}]
                     [:password :password {:class "pass"}]]
                    {:username "bad" :password "bad"})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:div
                  [:p {:class "username-error"} "bad"]
                  [:textarea {:class "usern" 
                              :id :username :name :username} "&lt;i&gt;"]]
                 [:div
                  [:p {:class "password-error"} "bad"]
                  [:input {:type :password :id "password" :class "pass"
                           :name "password" :value ""}]]])
       
       (fact "should create get form"
             (fform [:get "/login"]
                    [[:text :search {:value "<i>" :placeholder "Search"}]]
                    {:username "bad" :password "bad"})
             => [:form {:action (java.net.URI. "/login") :method "GET"}
                 [:input {:type :text :id "search" :name "search" :value "&lt;i&gt;"
                          :placeholder "Search"}]]))

(facts "fform - should not display error if :no-errors is present"
       (fact "shouldn't display errors"
             (fform [:post "/login"]
                    [[:textarea :username {:class "usern" :value "<i>"}]
                     [:password :password {:class "pass"}]]
                    {:no-errors true :username "bad" :password "pass"})
             => [:form {:action (java.net.URI. "/login") :method "POST"}
                 [:textarea {:class "usern" 
                             :id :username :name :username} "&lt;i&gt;"]
                 [:input {:type :password :id "password" :class "pass"
                          :name "password" :value ""}]]))




