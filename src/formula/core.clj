(ns formula.core
  (:require [trust.escape :refer [escape-html]]
            [hiccup.util :refer [to-uri]]
            [hiccup.def :refer [defelem]]
            [hiccup.form :refer [hidden-field select-options]]))

(defn- input-field
  "For different input fields"
  [type name attrs]
  [:input (conj {:type type
                 :name name
                 :id name
                 :value nil} attrs)])

(defn- display-error
  "Displays errors if map contains a message"
  [attr-key field attrs errors]
  (let [str-name (name attr-key)
        wrap (:wrap attrs)
        wrap-in (:wrap-in errors)
        wrap-fields (:wrap-fields errors)
        wrap-errors (:wrap-errors errors)
        field-no-error (if wrap [wrap field] field)
        field-error (if wrap-in field field-no-error)]
    (if (attr-key errors)
      [(if (and wrap wrap-in) wrap (or wrap-fields :div))
       field-error
       [(or wrap-errors :p) {:class (str str-name "-error")}
        (attr-key errors)]]
      field-no-error)))

(defelem generic-input
  "Generic input builder used for
   text, password, email, checkbox, radio, hidden, file"
  [type attr-key attrs & [errors]]
  (let [str-name (name attr-key)
        m-attrs (dissoc attrs :wrap)
        field (input-field type str-name m-attrs)]
    (display-error attr-key field attrs errors)))

(defelem text-area
  "Creates text area element - escapes value"
  [_ attr-key attrs & [errors]]
  (let [m-attrs (dissoc attrs :value :wrap)
        field [:textarea (conj {:name attr-key
                                :id attr-key} m-attrs)
               (escape-html (:value attrs))]]
    (display-error attr-key field attrs errors)))

(defelem button
  "Creates button element"
  [_ attr-key attrs & [errors]]
  (let [m-attrs (dissoc attrs :value :wrap)
        field [:button (conj {:type "button" :id attr-key} m-attrs)
               (:value attrs)]
        field (if (:wrap attrs) [(:wrap attrs) field] field)]
    field))

(defelem drop-down
  "Creates a drop down using the <select> tag"
  [_ attr-key attrs & [errors]]
  (let [m-attrs (dissoc attrs :selected :options :wrap)
        field [:select {:name attr-key :id attr-key}
               (select-options (:options attrs) (:selected attrs))]]
    (display-error attr-key field attrs errors)))

(def field-map
  "Functions for specific fields.
   - Options available but not include are
     text, password, email, checkbox, radio, hidden, file"
  {:textarea text-area :dropdown drop-down :button-tag button})

(defelem form-fields
  "Calls field functions"
  [field-vec errors]
  (let [deal (fn [[field attr-key attrs]]
               ((or (field field-map) generic-input) field attr-key attrs errors))
        body (map deal field-vec)]
    (if (:generic-error errors)
      (concat (seq [[:p {:class "generic-error"} (:generic-error errors)]])
              body)
      body)))

(defelem fform
  "Creates a form that points to a particular method and route.
   Also creates listed fields and displays errors"
  
  [[method action] field-vec & [errors]]
  
  (let [method-str (.toUpperCase (name method))
        action-uri (to-uri action)
        errors (if (:no-errors errors) nil errors)
        ]
    
    (-> (if (contains? #{:get :post} method)
          [:form {:method method-str, :action action-uri}]
          [:form {:method "POST", :action action-uri}
           (hidden-field "_method" method-str)])
        (concat (form-fields field-vec errors))
        (vec))))



