(ns formula.validation-test
  (:use clojure.test
        formula.validation
        hiccup.core))

; present = any value is present
; confirmation = matches another field
; exclusion = not included in list
; format = match certain regex
; inclusion = included in list
; length = within a certain length ... max and/or min
; numericality = only integers
; uniqueness = value is unique ; case_sensitive
; options allow_nil allow_blank message
; make their own function


;(confirm :confirmation [:password] user)
;(present :username user)

;((call :present) :password user)

