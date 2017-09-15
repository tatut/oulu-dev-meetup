(ns livecloud.polls
  (:require [specql.core :as specql :refer [define-tables]]
            [specql.rel :as rel]
            [specql.op :as op]
            [hiccup.core :refer [html]]
            [portkey.core :as pk]
            [clojure.java.jdbc :as jdbc]
            [livecloud.pie :as pie]
            [livecloud.deploy :refer [deploy!]]
            [livecloud.show-url :as url]))

;; Luetaan yhteystiedot kantaan paikallisesta tiedostosta
(def rds (-> "rds" slurp read-string))


(define-tables rds
  ["poll" :poll/polls
   {:poll/options (rel/has-many :poll/id :option/options :option/poll-id)}]
  ["option" :option/options]
  ["answer" :answer/answers]
  ["result" :result/result]
  ["poll-results" :poll/poll-results])

(def poll-with-options
  #{:poll/id :poll/title [:poll/options #{:option/option :option/id}]})


(defn poll-by-id [id]
  (first (specql/fetch rds :poll/polls poll-with-options {:poll/id id})))

(defn poll-results [id]
  (first (specql/fetch rds :poll/poll-results
                       (specql/columns :poll/poll-results)
                       {:poll/id id})))

(defn create-poll! [title description & options]
  (let [poll (specql/insert! rds :poll/polls
                             {:poll/title title
                              :poll/description description})]
    (doseq [option options]
      (specql/insert! rds :option/options
                      {:option/option option
                       :option/poll-id (:poll/id poll)}))
    poll))

(defn page [title & body-content]
  (html
   [:html
    [:head
     [:title title]]
    [:body
     body-content]]))

;; ihan alkuun peruskonsepti...

(defn hello-world [x]
  (page "Terve"
        [:pre {:style "font-size: 48pt;"}
         (str "Terve " x)]))

(deploy! hello-world "/terve?kuka={x}")

(def answer-url "place the URL of the answer lambda here, once deployed")

(defn poll-page [id]
  (let [poll (poll-by-id (Long/parseLong id))]
    (page (str "Poll: " (:poll/title poll))
          [:div
           [:h3 (:poll/title poll)]
           [:ul
            (for [{:option/keys [id option]} (:poll/options poll)]
              [:li [:a {:href (str answer-url
                                   "?poll=" (:poll/id poll)
                                   "&option=" id
                                   "&_=" (System/currentTimeMillis))}
                    option]])]])))

(deploy! poll-page "/kysely?poll={id}")


(defn results [poll-id]
  (let [{:poll/keys [title results]} (poll-results (Long/parseLong poll-id))]
    (page (str "POll results: " title)
          [:table
           [:thead
            [:tr
             [:td "Option"]
             [:td "Percentage"]
             [:td "Votes"]]]
           [:tbody
            (for [{:result/keys [option percentage votes]} results]
              [:tr
               [:td option]
               [:td (format "%.1f%%" percentage)]
               [:td votes]])]]
          (pie/pie {:width 300 :height 300 :radius 120
                    :show-text true}
                   (into {}
                         (map (juxt :result/option :result/votes))
                         results)))))

(deploy! results "/tulokset?poll={poll-id}")

(defn answer [poll-id option-id]
  (let [poll (poll-by-id (Long/parseLong poll-id))
        option-id (Long/parseLong option-id)
        option (some #(when (= (:option/id %) option-id) %)
                     (:poll/options poll))]
    (specql/insert! rds
                    :answer/answers
                    {:answer/poll-id (:poll/id poll)
                     :answer/option-id (:option/id option)})
    (results poll-id)))

(deploy! answer "/vastaa?poll={poll-id}&option={option-id}")
