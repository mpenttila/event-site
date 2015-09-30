(ns user
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [event-site.system :as system]
            [meta-merge.core :refer [meta-merge]]
            [clojure.edn :as edn]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]))

(reloaded.repl/set-init! #(system/new-system (meta-merge (edn/read-string (slurp "config.edn"))
                                                         (if (.exists (io/file "config-local.edn"))
                                                           (edn/read-string (slurp "config-local.edn"))
                                                           {}))))
