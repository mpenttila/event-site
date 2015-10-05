(ns ui.content.example)

(def pages [[:home [:div
                    [:h1 "Welcome to my event!"]]]
            [:register [:div
                        [:h1 "Register"]
                        [:register-form]]]
            [:about [:div
                     [:h1 "About me"]]]
            [:contact [:div
                       [:h1 "How to reach me"]]]])
