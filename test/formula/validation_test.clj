(ns formula.validation-test
  (:use clojure.test
        formula.validation
        midje.sweet
        hiccup.core))

(def user-m {:username "joe" :password "abcdef" :confirmation "abcdef"
             :gender "male" :age nil :nickname "" :friends 30})


(facts "message - use format for custom message or default"
       (fact "no custom should return default"
             (message :sleepy nil "I am so %s") => "I am so sleepy")
       
       (fact "with custom should return custom message."
             (message :excited "I am so %s" "nada") => "I am so excited"))


(facts "present - should be nil or validation error with message"
      (fact "error when not present"
            (present :nope user-m) => {:nope "nope must be present"})
      
      (fact "nil when present"
            (present :username user-m) => nil)
      
      (fact "custom message should show when provided"
            (present :nope user-m {:nope {:present "where is %s"}})
            =>{:nope "where is nope"}))


(facts "allow-nil - should be nil or validation error with message"
       (fact "return nil when not nil"
             (allow-nil :username false user-m) => nil)
       
       (fact "return nil with truthy value"
             (allow-nil :username :t user-m) => nil)
       
       (fact "return nil when allow-nil is true"
             (allow-nil :username true (assoc user-m :username nil)) => nil)
       
       (fact "error when nil"
             (allow-nil :username false (conj user-m {:username nil}))
             => {:username "username can't be empty"})
       
       (fact "show custom message when provided"
             (allow-nil :username false (assoc user-m :username nil)
                        {:username {:allow-nil "%s shouldn't be nil"}})))


(facts "allow-blank - should be nil or validation error with message"
       (fact "return nil when not blank"
             (allow-blank :username false user-m) => nil)
       (fact "return nil when value is persent"
             (allow-blank :username false user-m) => nil)
       
       (fact "return nil when allow-blank is true"
             (allow-blank :username true (assoc user-m :username "")) => nil)
       
       (fact "return nil when value is nil"
             (allow-blank :username false (conj user-m {:username nil}))
             => nil)
       (fact "error when blank"
             (allow-blank :username nil (assoc user-m :username ""))
             => {:username "username can't be blank"})
       (fact "error when blank but spaces"
             (allow-blank :username false (conj user-m {:username "  "}))
             => {:username "username can't be blank"})
       (fact "show custom message when provided"
             (allow-blank :username false (assoc user-m :username "")
                          {:username {:allow-nil "%s shouldn't be nil"}})))

(facts "confirm - should be nil or validation error with message"
       (fact "return nil when confirm matches"
             (confirm :confirmation [:password] user-m) => nil)
       (fact "error when doesn't match"
             (confirm :confirmation [:username] user-m)
             => {:confirmation "confirmation must match username"})
       (fact "error testing two and one doesn't match"
             (confirm :confirmation [:password :username] user-m)
             => {:confirmation "confirmation must match username"})
       (fact "return nil when all match"
             (confirm :confirmation [:password :username] (conj user-m {:username
                                                                        "abcdef"}))
             => nil)
       (fact "show custom message when provided"
             (confirm :confirmation [:username] user-m {:confirmation
                                                        {:confirm "no match"}})
             => {:confirmation "no match"}))

(facts "exclusion - should be nil or validation error with message"
      (fact "return nil when not in coll"
            (exclusion :username ["hi" "you"] user-m) => nil)
      (fact "error when in coll"
            (exclusion :username ["hi" "joe"] user-m)
            => {:username "username is not an acceptable term"})
      (fact "show custom message when provided"
            (exclusion :username ["joe"] user-m {:username
                                                {:exclusion "in list"}})
            => {:username "in list"}))

(facts "inclusion - should be nil or validation error with message"
      (fact "return nil when in coll"
            (inclusion :username ["who" "joe"] user-m) => nil)
      (fact "error when not in coll"
            (inclusion :username ["hi" "so"] user-m)
            => {:username "username is not an acceptable term"})
      (fact "show custom message when provided"
            (inclusion :username ["hi"] user-m {:username
                                                {:inclusion "where's the %s"}})
            => {:username "where's the username"}))

(facts "formats - should be nil or validation error with message"
       (fact "return nil when matches regex"
             (formats :username #"\d+" (conj user-m {:username "384"})) => nil)
       (fact "error when doesn't match regex"
             (formats :username #"\d+" user-m) =>
             {:username "username is not the correct format"})
       (fact "show custom error message when provided"
             (formats :username #"\d+" user-m {:username
                                               {:confirm "nada"
                                                :formats "no match"}})
             => {:username "no match"}))

(facts "length - should be nil or validation error with message"
      (fact "return nil when the correct length"
            (length :username {:min 3 :max 8} user-m) => nil)
      
      (fact "error when not correct length"
            (length :username {:min 8 :max 20} user-m)
            {:username "username should be between 8 and 20 characters"})
      
      (fact "error with only min"
            (length :username {:min 4} user-m)
            => {:username "username should be at least 4 characters"})
      
      (fact "error with only max"
            (length :username {:max 2} user-m)
            => {:username "username should be at most 2 characters"})
      
      (fact "show custom error message when provided"
            (length :username {:max 2} user-m {:username
                                               {:length "gotta be longer"}})
            => {:username "gotta be longer"})
      (fact "when both present but error with min"
            (length :username {:min 8 :max 20} user-m)
            => {:username "username should be between 8 and 20 characters"})
      (fact "when both present but error with max"
            (length :username {:min 1 :max 2} user-m)
            => {:username "username should be between 1 and 2 characters"}))

(facts "unique - should be nil or validation error with message"
      (fact "return nil when value is unique"
            (unique :username #(= % "joe") user-m)
            => {:username "username must be unique"})
      (fact "error when value is not unique"
            (unique :username #(= % "joe") (conj user-m {:username "bob"})) => nil)
      (fact "custom message when provided"
            (unique :username #(= % "joe") user-m {:username
                                                   {:unique "not unique"}})
            => {:username "not unique"}))

(facts "sender-loop vali function - should direct rule"
      (let [vali (fn [r & msg] (if (map? r)
                       ((call (apply key r)) :username (apply val r) user-m msg)))]
        (fact "should return nil"
              (vali :present ) => nil)
        (fact "should return error"
              (vali {:unique #(= % "joe")} {})
              => {:username "username must be unique"})
        (fact "should return nil"
              (vali {:length {:min 2 :max 5}} {}) => nil)))

(facts "numbers - should return an error or {}"
       (fact "should return {} when all is good"
             (numbers :friends {:gt 10 :lt 40 :even true :lte 30 :only-int true
                                :gte 30} user-m)
             => {})
       (fact "should return first error - odd"
             (numbers :friends {:odd true :gt 31} user-m)
             => {:friends "friends must be odd"})
       (fact "return error gt"
             (numbers :friends {:eq 30 :lt 40 :even true :only-int true :gt 31}
                      user-m)
             => {:friends "friends must be greater than 31"})
       (fact "return error gte"
              (numbers :friends {:eq 30 :lt 40 :even true :only-int true :gte 31}
                       user-m)
             => {:friends "friends must be greater or equal to 31"})
       (fact "return error lt"
              (numbers :friends {:eq 30 :lte 40 :even true :only-int true :lt 29}
                       user-m)
             => {:friends "friends must be less than 29"})
       (fact "return error lte"
              (numbers :friends {:eq 30 :lt 40 :even true :only-int true :lte 29}
                       user-m)
             => {:friends "friends must be less than or equal to 29"})
       (fact "return error eq"
              (numbers :friends {:lt 40 :even true :only-int true :lte 29 :eq 31}
                       user-m)
             => {:friends "friends must be equal to 31"})
       (fact "return error only-int"
              (numbers :friends {:lt 40 :even true :lte 31 :eq 30.0 :only-int true}
                       (conj user-m {:friends 30.0}))
             => {:friends "friends must be an integer"})
       (fact "return error even"
              (numbers :friends {:lte 31 :eq 29 :only-int true :lt 40 :even true}
                       (conj user-m {:friends 29}))
             => {:friends "friends must be even"})
       (fact "should return custom message if provided"
              (numbers :friends {:gt 31} user-m {:friends {:numbers "%s must be >"}})
             => {:friends "friends must be >"})
       (fact "error when unrecognized value"
              (numbers :friends {:gt 31} (conj user-m {:friends "bad-value"}))
              => {:friends "friends must be a number"}))


(facts "sender-loop loop"
      (let [vali (fn [r & msg] (if (map? r)
                          ((call (apply key r)) :username (apply val r) user-m msg)
                          ((call r) :username user-m msg)))
            send-loop (fn [rules & msg] (loop [rules rules errors {}]
                                    (if (or (empty? rules) (seq errors))
                                      errors
                                      (recur (rest rules)
                                             (conj errors (vali (first rules)
                                                                msg))))))]
        (fact "should return nil"
              (send-loop [:present]) => {})
        (fact "should return present error"
              (send-loop [:present {:unique #(= % "joe")}]) => {:username
                                                       "username must be unique"})))

(facts "sender-loop - should return errors or {}"
      (fact "should return {} when all is good"
            (sender-loop :username [:allow-nil] user-m) => {})
      (fact "should be return unique error"
            (sender-loop :username [:present {:unique #(= % "joe")}] user-m)
            => {:username "username must be unique"})
      (fact "should return custom error when provided"
            (sender-loop :username [{:formats #"\d+"}] user-m {:username
                                                               {:formats "bad"}})
            => {:username "bad"})
      (fact "should only return the first error"
            (sender-loop :username [{:unique #(= % "joe")} {:formats #"\d+"}]
                         user-m) => {:username "username must be unique"}))


(facts "default-check - should return errors or {}"
       (fact "should return {} when all is good"
             (default-check :username [:allow-nil] user-m) => {})
       (fact "should return an error when nil"
             (default-check :username [:present] (conj user-m {:username nil}))
             => {:username "username can't be empty"})
       (fact "should return an error when empty?"
             (default-check :username [nil :present] (conj user-m {:username ""}))
             => {:username "username can't be blank"})
       (fact "should return {} if no field or :present"
             (default-check :business [{:exlusion ["groupon"]}] user-m)
             => {})
       (fact "should return error when nil"
             (default-check :age [{:length {:min 7 :max 9}}] user-m)
             => {:age "age can't be empty"})
       (fact "error return with no field and :present"
             (default-check :business [:present {:exclusion ["groupon"]}] user-m)
             => {:business "business must be present"})
       (fact "nil should work if allow-nil is in rules"
             (default-check :username [:present :allow-nil]
               (conj user-m {:username nil})) => {}))

(facts "sender - should return errors or {}"
      (fact "should return {} when all is good"
            (sender :username [:present] user-m) => {})
      (fact "should return {} when all is good"
            (sender :username [:allow-nil] user-m) => {})
      (fact "error when wrong format"
            (sender :username [:present {:formats #"\d+"}] user-m)
            => {:username "username is not the correct format"})
      (fact "custom message should show when provided"
            (sender :username [:present {:formats #"\d+"}] user-m
                    {:username {:formats "wrong"}})
            => {:username "wrong"}))


(facts "validate - When all is good {} should be returned."
       (fact "should return {} when all good"
             (validate [[:password :present {:length {:min 6 :max 30}}
                         {:formats #"\w+"}]
                        [:username :present {:unique #(= % "nada")}]
                        [:confirmation :present {:confirm [:password]}]
                        [:gender :present {:inclusion ["female" "male"]}]
                        [:business {:exclusion ["facebook" "windows"]}]
                        [:age :allow-nil]
                        [:friends {:numbers {:gt 10 :lt 40 :even true :lte 30
                                             :only-int true :gte 30}}]
                        [:nickname :allow-blank]] user-m)
             => {}))

(facts "validate - When all is bad errors should be returned"
       (fact "should get errors"
             (validate [[:password :present {:length {:min 8}}]
                        [:age {:length {:min 3 :max 10}}]
                        [:username :present {:unique #(= % "joe")}]
                        [:gender {:inclusion ["nada"]}]
                        [:school :present]
                        [:nickname {:exclusion ["admin"]}]] user-m)
             => {:password "password should be at least 8 characters"
                 :age "age can't be empty"
                 :username "username must be unique"
                 :gender "gender is not an acceptable term"
                 :school "school must be present"
                 :nickname "nickname can't be blank"}))

