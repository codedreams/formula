# Formula
Formula is a clojure library for html forms


Install
-------
To test formula out, 
add the following dependency to your `project.clj` file:

    [formula "1.0.0-alpha"]

###Formula's Forms


Formula is built using hiccup and clojure.  It is designed to make form easy to build, and validate.  The forms have reusability by taking error messages and displaying with the fields (see designed flow, below).  A basic form would look like this

```clojure
(fform [:post "/signup"]
       [[:text :username {:placeholder "Enter Username"}]
        [:password :password {:class "pass"}]]
       error-message-map)
```

This is post request to the url "/signup".  The first field is a text field for username, with an optional placeholder attribute.  The second field is a password field for the password, with an optional class.  There is also an optional argument
which will display any errors for a particular field.  An example of error-message-map would be

```clojure
(def error-message-map {:username "pick another username"})
```

The above error message map will display an error of "pick another username" in a div with the username.

### Form 

Forms can take any number of attributes or none at all.  The function for calling forms is **fform**
it takes a method and action, a vector of fields, and an optional error message map.  If the error messages are present, they will be display inside a div with the specific field.


###Validation
Formula has a built in validation library, but you can use whatever validation library you want.  Formula only requires that the errors be returned as a map with the field name and the message e.g.

```clojure
{:password "password should be at least 8 characters"
 :age "age can't be empty"
 :username "username must be unique"
 :gender "gender is not an acceptable term"
 :school "school must be present"
 :nickname "nickname can't be blank"}
 ```
 
 If you were to use Formula's built in validation the rules would look like this
 
 ```clojure
 (def user-m {:username "joe" :password "abcdef" :confirmation "abcdef"
             :gender "male" :age nil :nickname "" :friends 30})
 ```


```clojure
(validate [[:password :present {:length {:min 8}}]
           [:age {:length {:min 3 :max 10}}]
           [:username :present {:unique #(= % "joe")}]
           [:gender {:inclusion ["nada"]}]
           [:school :present]
           [:nickname {:exclusion ["admin"]}]] 
           user-m)
 ```
 
 The validate function takes a vector of rules for a specific field.  The field is the first thing in the vector.  After the
 field there are rules that should be checked against the given value.  Formula's validation is designed to stop 
 after the first error message, so other rules won't get executed if an error is present.  After the vector, the map being 
 validated should be included.  The last argument is optional, it is a map of custom error message which will 
 replace the defaults.
 
 The custom error message should something like this
 
 ```clojure
 {:password {:present "password must be present"
             :length "The %s has to be hard to guess"}
  :school {:present "School is cool"}}
 ```
 
 This replace the default error messages for password (present and length) and school (present).  You can even
 enter %s to get access to the field being validated.  So in the example above %s would be password.
 

### Designed Flow 

Formula uses the same form to display fields, and display errors.  The flow is designed to go as follows 

* **Form is displayed to user**

* **User submits the form**

* **Params map goes into validation**

* **If there are errors in the form, it goes back into the form for display**

* **This process continues until no errors are present**

***
***

*Below is a rough diagram of this process

![Flow icon](http://i42.tinypic.com/30vgdn5.png)

***
***

###Why

Basic web development tasks should be easy to accomplish.  Things like form display and validation should only take a few minutes to implement, leaving more time for hard problems.

###Todo

* Make a list of all validation and field options available.
* Implement in an actual application to see if work flow is as desired.
* Improve parts of code and readme.



