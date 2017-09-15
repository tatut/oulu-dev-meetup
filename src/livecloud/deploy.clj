(ns livecloud.deploy
  (:require [livecloud.show-url :as show-url]))

(defmacro deploy! [function path]
  `(let [result#
         (pk/mount! ~function ~path
                    :keeps #{org.postgresql.Driver}
                    :vpc-config {:subnet-ids #{"subnet-77c1841e"
                                               "subnet-7f108804"}
                                 :security-group-ids #{"sg-b7b0a9dc"}}
                    :content-type "text/html")]
     (println "URL: " (:url result#))
     (:url result#)))
