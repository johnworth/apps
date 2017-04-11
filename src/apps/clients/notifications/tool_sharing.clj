(ns apps.clients.notifications.tool-sharing
  (:use [apps.clients.notifications.common-sharing]
        [medley.core :only [remove-vals]])
  (:require [clojure.string :as string]))

(def notification-type "tools")
(def singular "tool")
(def plural "tools")

(defn- format-tool
  [response]
  (remove-vals nil? (select-keys response [:tool_id :tool_name])))

(defn- format-payload
  [action responses]
  {:action action
   :tools  (map format-tool responses)})

(defn- format-notification
  [recipient formats action sharer sharee responses]
  (when (seq responses)
    (let [response-desc  (string/join ", " (map :tool_name responses))
          response-count (count responses)]
      {:type    notification-type
       :user    recipient
       :subject (format-subject formats singular plural action sharer sharee response-desc response-count)
       :message (format-message formats singular plural action sharer sharee response-desc response-count)
       :payload (format-payload action responses)})))

(defn- format-sharer-notification
  [formats action sharer sharee responses]
  (format-notification sharer formats action sharer sharee responses))

(defn- format-sharee-notification
  [formats action sharer sharee responses]
  (format-notification sharee formats action sharer sharee responses))

(defn format-sharing-notifications
  "Formats sharing notifications for tools."
  [sharer sharee responses]
  (let [responses (group-by :success responses)]
    (remove nil?
            [(format-sharer-notification sharer-success-formats share-action sharer sharee (responses true))
             (format-sharee-notification sharee-success-formats share-action sharer sharee (responses true))
             (format-sharer-notification failure-formats share-action sharer sharee (responses false))])))

(defn format-unsharing-notifications
  "Formats unsharing notifications for tools."
  [sharer sharee responses]
  (let [responses (group-by :success responses)]
    (remove nil?
            [(format-sharer-notification sharer-success-formats unshare-action sharer sharee (responses true))
             (format-sharer-notification failure-formats unshare-action sharer sharee (responses false))])))
