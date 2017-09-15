(ns livecloud.show-url
  (:import (net.glxn.qrgen.javase QRCode)
           (net.glxn.qrgen.core.image ImageType))
  (:require [clojure.java.shell :as sh]
            [org.httpkit.client :as http]
            [clojure.string :as str]))

(defn url->qr-image-file [url]
  (-> url
      QRCode/from
      (.to ImageType/PNG)
      (.withSize 512 512)
      .file))

(defn open-image-file [file]
  (sh/sh "open" (.getAbsolutePath file)))

(defn show-url [url]
  (-> url
      url->qr-image-file
      open-image-file))

(defn url->short-url [url]
  (let [body (:body @(http/get "http://shortify.site/api/url/shorten/"
                               {:query-params {"url" url}}))]
    (when (and body (str/starts-with? body "1:"))
      (subs body 2))))

(defn print-short-url-and-show-qr-code [url]
  (println "URL: " (url->short-url url))
  (show-url url))
