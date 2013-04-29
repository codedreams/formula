(ns formula.validation-test
  (:use clojure.test
        formula.validation
        midje.sweet
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

(def user-m {:username "joe" :password "abcdef" :confirmation "abcdef"})

(fact "call - should take string and make function call"
      ((call "str") 1) => "1"
      ((call "nil?") nil) => true)

(facts "message - use format for custom message or default"
       (fact "no custom should return default"
             (message :sleepy nil "I am so %s") => "I am so sleepy")
       (fact "with custom should return custom message"
             (message :excited "I am so %s" "nada") => "I am so excited"))

(fact "present - should be nil or validation error with message"
      (fact "error when not present"
            (present :nope user-m) => {:nope "nope must be present"})
      (fact "nil when present"
            (present :username user-m) => nil)
      (fact "custom message should show when provided"
            (present :nope user-m {:nope {:present "where is %s"}})
            =>{:nope "where is nope"}))

(fact "allow-nil - should be nil or validation error with message"
      (let [user-nil (conj user-m {:username nil})]
        (fact "return nil when not nil"
              (allow-nil :username false user-m) => nil)
        (fact "return nil when allow-nil is true"
              (allow-nil :username true user-nil) => nil)
        (fact "error when nil"
              (allow-nil :username false user-nil)
              => {:username "username can't be empty"})
        (fact "show custom message when provided"
              (allow-nil :username false user-nil
                         {:username {:allow-nil "%s shouldn't be nil"}}))))

(fact "allow-blank - should be nil or validation error with message"
      (let [user-blank (conj user-m {:username ""})]
        (fact "return nil when not blank"
              (allow-blank :username false user-m) => nil)
        (fact "return nil when allow-blank is true"
              (allow-blank :username true user-blank) => nil)
        (fact "error when blank"
              (allow-blank :username false user-blank)
              => {:username "username can't be blank"})
        (fact "show custom message when provided"
              (allow-blank :username false user-blank
                         {:username {:allow-nil "%s shouldn't be nil"}}))))

(fact "confirm - should be nil or validation error with message"
      (let [same-values (conj user-m {:username "abcdef"})]
        (fact "return nil when confirm matches"
              (confirm :confirmation [:password] user-m) => nil)
        (fact "error when doesn't match"
              (confirm :confirmation [:username] user-m)
              => {:confirmation "confirmation must match username"})
        (fact "error testing two and one doesn't match"
              (confirm :confirmation [:password :username] user-m)
              => {:confirmation "confirmation must match username"})
        (fact "return nil when all match"
              (confirm :confirmation [:password :username] same-values)
              => nil)
        (fact "show custom message when provided"
              (confirm :confirmation [:username] user-m {:confirmation
                                                         {:confirm "no match"}})
              => {:confirmation "no match"})))

(fact "exclusion - should be nil or validation error with message"
      (fact "return nil when not in coll"
            (exclusion :username ["hi" "you"] user-m) => nil)
      (fact "error when in coll"
            (exclusion :username ["hi" "joe"] user-m)
            => {:username "username is not an acceptable term"})
      (fact "show custom message when provided"
            (exclusion :username ["joe"] user-m {:username
                                                {:exclusion "in list"}})
            => {:username "in list"}))

(fact "inclusion - should be nil or validation error with message"
      (fact "return nil when in coll"
            (inclusion :username ["who" "joe"] user-m) => nil)
      (fact "error when not in coll"
            (inclusion :username ["hi" "so"] user-m)
            => {:username "username is not an acceptable term"})
      (fact "show custom message when provided"
            (inclusion :username ["hi"] user-m {:username
                                                {:inclusion "where's the %s"}})
            => {:username "where's the username"}))

(fact "formats - should be nil or validation error with message"
      (let [user-reg (conj user-m {:username "384"})]
        (fact "return nil when matches regex"
              (formats :username #"\d+" user-reg) => nil)
        (fact "error when doesn't match regex"
              (formats :username #"\d+" user-m) =>
              {:username "username is not the correct format"})
        (fact "show custom error message when provided"
              (formats :username #"\d+" user-m {:username
                                                {:confirm "nada"
                                                 :formats "no match"}})
              => {:username "no match"})))

(fact "length - should be nil or validation error with message"
      (fact "return nil when the correct length"
            (length :username {:min 3 :max 8} user-m) => nil)
      (fact "error when not correct length"
            (length :username {:min 8 :max 20} user-m)
            {:username "username should be between 8 and 20 characters"})
      (fact "error with only min"
            (length :username {:min 3} user-m)
            => {:username "username should be at least 3 characters"})
      (fact "error with only max"
            (length :username {:max 8} user-m)
            => {:username "username should be at most 3 characters"})
      (fact "show custom error message when provided"
            (length :username {:max 8} user-m {:username
                                               {:length "gotta be long"}})
            => {:username "gotta be long"}))



