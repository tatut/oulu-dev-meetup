(defproject livecloud "0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [com.github.kenglxn.qrgen/javase "2.3.0"]
                 [http-kit "2.2.0"]
                 [org.clojure/java.jdbc "0.7.0-alpha3"]
                 [specql "0.7.0-alpha1"]
                 [hiccup "1.0.5"]
                 [portkey "0.1.0-SNAPSHOT"]]
  :repositories [["jitpack" "https://jitpack.io"]]
  :profiles {:dev {:dependencies [[com.opentable.components/otj-pg-embedded "0.7.1"]]}})
