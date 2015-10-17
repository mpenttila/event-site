(defproject event-site "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [com.stuartsierra/component "0.3.0"]
                 [reagent "0.5.1"]
                 [compojure "1.4.0"]
                 [duct "0.4.2"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.3.0"]
                 [com.novemberain/monger "3.0.1"]
                 [prismatic/plumbing "0.5.0"]
                 [prismatic/schema "1.0.1"]
                 [ring-transit "0.1.3"]
                 [cljs-ajax "0.5.0"]
                 [com.draines/postal "1.11.3"]]
  :plugins [[lein-less "1.7.5"]]
  :main ^:skip-aot event-site.main
  :target-path "target/%s/"
  :resource-paths ["resources" "target/cljsbuild"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljs" "src/cljc"]
                             :compiler {:main ui.app
                                        :output-to "resources/private/main.js"
                                        :asset-path "/out"}
                             :figwheel { :on-jsload "ui.app/start" }}
                       :prod {:source-paths ["src/cljs" "src/cljc"]
                              :compiler {:main ui.app
                                         :output-to "resources/private/main.js"
                                         :optimizations :advanced
                                         :closure-extra-annotations #{"api" "observable"}}}}}
  :figwheel {:nrepl-port 7889
             :server-port 3450
             :css-dirs ["resources/public/css"]}
  :uberjar-name "event-site.jar"
  :source-paths ["src/clj" "src/cljc"]
  :repl-options {:init-ns user}
  :clean-targets ^{:protect false} ["resources/private/main.js"
                                    "resources/private/out"
                                    :target-path]
  :less {:source-paths ["src/less"]
         :target-path "resources/public/css"}
  :profiles {:uberjar {:aot :all
                       :prep-tasks ^:replace ["clean"
                                              ["less" "once"]
                                              ["cljsbuild" "once" "prod"]
                                              "javac"
                                              "compile"]}
             :dev {:source-paths ["dev"]
                   :dependencies [[reloaded.repl "0.2.0"]
                                  [org.clojure/tools.nrepl "0.2.11"]]
                   :plugins [[lein-cljsbuild "1.1.0"]
                             [lein-figwheel "0.4.0" :exclusions [org.clojure/clojure
                                                                 org.clojure/clojure org.codehaus.plexus/plexus-utils]]]}})
