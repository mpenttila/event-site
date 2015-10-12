(ns ui.content.example)

(def pages [{:id "home"
             :style {:background-image "url('http://lorempixel.com/1600/800')"}
             :markup [:div
                      [:h1 "Welcome to my event!"]]}
            {:id "register"
             :style {:background-image "url('http://lorempixel.com/1700/900')"}
             :markup [:div
                      [:h1 "Register"]
                      [:register-form]]}
            {:id "about"
             :style {:background-color "#00a8ff"}
             :markup [:div
                      [:h1 "About me"]]}
            {:id "contact"
             :style {:background-color "#4f0322"}
             :markup [:div
                      [:h1 "How to reach me"]]}])